package store;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.Admin;
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import model.User;

public class UserStore {
    public static final String FILE_PATH_PROPERTY = "user.store.path";

    public static List<TA> getTAList() {
        List<Course> availableCourses = CourseStore.getCourseList();
        return getTaUsersForCourses(availableCourses);
    }

    public static void saveUser(User user) {
        String line = toLine(user);
        Path filePath = resolveFilePath();

        try {
            ensureParentDirectoryExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
            fw.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User validateUser(String password, String role, String email) {
        return findUser(password, email, role, true);
    }

    public static User validateUser(String password, String email) {
        return findUser(password, email, null, false);
    }

    public static boolean isEmailRegistered(String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine != null && email.equals(parsedLine.email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateAppliedCourseIds(TA ta) {
        updateUserLine("TA", ta.getEmail(), toTaLine(ta));
    }

    public static void updateTaProfile(TA ta) {
        updateUserLine("TA", ta.getEmail(), toTaLine(ta));
    }

    public static void updateMoProfile(Mo mo) {
        updateUserLine("Mo", mo.getEmail(), toMoLine(mo));
    }

    public static void updateOwnedCourseIds(Mo mo) {
        updateUserLine("Mo", mo.getEmail(), toMoLine(mo));
    }

    static List<TA> getTaUsersForCourses(List<Course> availableCourses) {
        List<TA> taUsers = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return taUsers;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null || !"TA".equals(parsedLine.role)) {
                    continue;
                }

                User user = buildUser(parsedLine, availableCourses);
                if (user instanceof TA ta) {
                    taUsers.add(ta);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return taUsers;
    }

    private static User findUser(String password, String email, String role, boolean matchRole) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        List<Course> availableCourses = CourseStore.getCourseList();

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null) {
                    continue;
                }

                boolean passwordMatches = password.equals(parsedLine.password);
                boolean emailMatches = email.equals(parsedLine.email);
                boolean roleMatches = !matchRole || role.equals(parsedLine.role);

                if (passwordMatches && emailMatches && roleMatches) {
                    return buildUser(parsedLine, availableCourses);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void updateUserLine(String role, String email, String replacementLine) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            try {
                ensureParentDirectoryExists(filePath);
                Files.write(filePath, List.of(replacementLine));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine != null && role.equals(parsedLine.role) && email.equals(parsedLine.email)) {
                    updatedLines.add(replacementLine);
                    updated = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!updated) {
            updatedLines.add(replacementLine);
        }

        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static User buildUser(ParsedUserLine parsedLine, List<Course> availableCourses) {
        User user = switch (parsedLine.role) {
            case "Admin" -> new Admin(parsedLine.password, parsedLine.email);
            case "TA" -> new TA(parsedLine.password, parsedLine.email);
            case "Mo" -> new Mo(parsedLine.password, parsedLine.email);
            default -> null;
        };

        if (user == null) {
            return null;
        }

        if (parsedLine.name != null && !parsedLine.name.isBlank()) {
            user.setName(parsedLine.name);
        }

        if (user instanceof TA ta) {
            ta.setCollege(parsedLine.taCollege);
            ta.setSkill(parsedLine.taSkill);
            if (!parsedLine.appliedCourseIds.isBlank()) {
                ta.setAppliedClasses(resolveCourses(parsedLine.appliedCourseIds, availableCourses));
            }
            if (!parsedLine.resumeMappings.isBlank()) {
                ta.setResumeSubmissions(resolveResumeSubmissions(parsedLine.resumeMappings, availableCourses));
            }
        }

        if (user instanceof Mo mo) {
            mo.setDegree(parsedLine.moDegree);
            mo.setCollege(parsedLine.moCollege);
            if (!parsedLine.ownedCourseIds.isBlank()) {
                mo.setOwnedCourses(resolveCourses(parsedLine.ownedCourseIds, availableCourses));
            }
        }

        return user;
    }

    private static ParsedUserLine parseUserLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
            return null;
        }

        ParsedUserLine parsedLine = new ParsedUserLine();
        parsedLine.name = parts[0];
        parsedLine.password = parts[1];
        parsedLine.role = parts[2];
        parsedLine.email = parts[3];

        if ("TA".equals(parsedLine.role)) {
            parseTaFields(parsedLine, parts);
        } else if ("Mo".equals(parsedLine.role)) {
            parseMoFields(parsedLine, parts);
        }

        return parsedLine;
    }

    private static void parseTaFields(ParsedUserLine parsedLine, String[] parts) {
        if (parts.length >= 8) {
            parsedLine.taCollege = parts[4];
            parsedLine.taSkill = parts[5];
            parsedLine.appliedCourseIds = parts[6];
            parsedLine.resumeMappings = parts[7];
            return;
        }

        if (parts.length >= 6) {
            parsedLine.appliedCourseIds = parts[4];
            parsedLine.resumeMappings = parts[5];
            return;
        }

        if (parts.length >= 5) {
            parsedLine.appliedCourseIds = parts[4];
        }
    }

    private static void parseMoFields(ParsedUserLine parsedLine, String[] parts) {
        if (parts.length >= 7) {
            parsedLine.moDegree = parts[4];
            parsedLine.moCollege = parts[5];
            parsedLine.ownedCourseIds = parts[6];
            return;
        }

        if (parts.length >= 5) {
            parsedLine.ownedCourseIds = parts[4];
        }
    }

    private static String toLine(User user) {
        if (user instanceof TA ta) {
            return toTaLine(ta);
        }
        if (user instanceof Mo mo) {
            return toMoLine(mo);
        }
        return toAdminLine(user);
    }

    private static String toAdminLine(User user) {
        return baseLine(user);
    }

    private static String toTaLine(TA ta) {
        return baseLine(ta) + "," + safe(ta.getCollege()) + "," + safe(ta.getSkill()) + ","
                + serializeAppliedCourseIds(ta) + "," + serializeResumeSubmissions(ta);
    }

    private static String toMoLine(Mo mo) {
        String degree = safe(mo.getDegree());
        String college = safe(mo.getCollege());
        String ownedCourseIds = serializeOwnedCourseIds(mo);
        if (degree.isBlank() && college.isBlank()) {
            return baseLine(mo) + "," + ownedCourseIds;
        }
        return baseLine(mo) + "," + degree + "," + college + "," + ownedCourseIds;
    }

    private static String baseLine(User user) {
        return safe(user.getName()) + "," + safe(user.getPassword()) + "," + safe(user.getRole()) + "," + safe(user.getEmail());
    }

    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "users.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "users.txt");
    }

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }

    private static String serializeOwnedCourseIds(Mo mo) {
        return mo.getOwnedCourses().stream()
                .map(Course::getId)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    private static String serializeAppliedCourseIds(TA ta) {
        return ta.getAppliedClasses().stream()
                .map(Course::getId)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    private static String serializeResumeSubmissions(TA ta) {
        return ta.getResumeSubmissions().stream()
                .filter(submission -> submission.getCourse() != null)
                .map(submission -> submission.getCourse().getId() + "@"
                        + submission.getResumeDirectory() + "@"
                        + submission.getStatus() + "@"
                        + submission.isReviewUnread())
                .collect(Collectors.joining("|"));
    }

    private static List<Course> resolveCourses(String courseIds, List<Course> availableCourses) {
        List<Course> resolvedCourses = new ArrayList<>();
        for (String courseId : courseIds.split("\\|")) {
            if (courseId == null || courseId.isBlank()) {
                continue;
            }
            for (Course course : availableCourses) {
                if (courseId.equals(course.getId())) {
                    resolvedCourses.add(course);
                    break;
                }
            }
        }
        return resolvedCourses;
    }

    private static List<ResumeSubmission> resolveResumeSubmissions(String resumeMappings, List<Course> availableCourses) {
        List<ResumeSubmission> submissions = new ArrayList<>();
        for (String mapping : resumeMappings.split("\\|")) {
            if (mapping == null || mapping.isBlank()) {
                continue;
            }

            String[] parts = mapping.split("@", 4);
            if (parts.length < 2) {
                continue;
            }

            String courseId = parts[0];
            String resumeDirectory = parts[1];
            int status = ResumeSubmission.STATUS_PENDING;
            boolean reviewUnread = false;

            if (courseId == null || courseId.isBlank() || resumeDirectory == null || resumeDirectory.isBlank()) {
                continue;
            }

            if (parts.length >= 3) {
                try {
                    status = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    status = ResumeSubmission.STATUS_PENDING;
                }
            }

            if (parts.length == 4) {
                reviewUnread = Boolean.parseBoolean(parts[3]);
            }

            for (Course course : availableCourses) {
                if (courseId.equals(course.getId())) {
                    submissions.add(new ResumeSubmission(course, resumeDirectory, status, reviewUnread));
                    break;
                }
            }
        }
        return submissions;
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ");
    }

    private static final class ParsedUserLine {
        private String name = "";
        private String password = "";
        private String role = "";
        private String email = "";
        private String taCollege = "";
        private String taSkill = "";
        private String moDegree = "";
        private String moCollege = "";
        private String ownedCourseIds = "";
        private String appliedCourseIds = "";
        private String resumeMappings = "";
    }
}
