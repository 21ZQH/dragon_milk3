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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User validateUser(String password, String role, String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        List<Course> availableCourses = CourseStore.getCourseList();

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    String college = extractCollege(r, parts);
                    String skill = extractSkill(r, parts);
                    String ownedCourseIds = extractOwnedCourseIds(r, parts);
                    String appliedCourseIds = extractAppliedCourseIds(r, parts);
                    String resumeMappings = extractResumeMappings(r, parts);
                    if (p.equals(password) && r.equals(role) && e.equals(email)) {
                        return buildUser(name, r, p, e, college, skill, ownedCourseIds, appliedCourseIds, resumeMappings, availableCourses);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User validateUser(String password, String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        List<Course> availableCourses = CourseStore.getCourseList();

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    String college = extractCollege(r, parts);
                    String skill = extractSkill(r, parts);
                    String ownedCourseIds = extractOwnedCourseIds(r, parts);
                    String appliedCourseIds = extractAppliedCourseIds(r, parts);
                    String resumeMappings = extractResumeMappings(r, parts);
                    if (p.equals(password) && e.equals(email)) {
                        return buildUser(name, r, p, e, college, skill, ownedCourseIds, appliedCourseIds, resumeMappings, availableCourses);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmailRegistered(String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String e = parts[3];
                    if (e.equals(email)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateAppliedCourseIds(TA ta) {
        updateTaLine(ta);
    }

    public static void updateTaProfile(TA ta) {
        updateTaLine(ta);
    }

    public static void updateOwnedCourseIds(Mo mo) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            saveUser(mo);
            return;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4 && "Mo".equals(parts[2]) && parts[3].equals(mo.getEmail())) {
                    updatedLines.add(toLine(mo));
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
            updatedLines.add(toLine(mo));
        }

        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                String[] parts = line.split(",", -1);
                if (parts.length >= 4 && "TA".equals(parts[2])) {
                    String name = parts[0];
                    String password = parts[1];
                    String email = parts[3];
                    String college = extractCollege("TA", parts);
                    String skill = extractSkill("TA", parts);
                    String ownedCourseIds = extractOwnedCourseIds("TA", parts);
                    String appliedCourseIds = extractAppliedCourseIds("TA", parts);
                    String resumeMappings = extractResumeMappings("TA", parts);

                    User user = buildUser(name, "TA", password, email, college, skill, ownedCourseIds, appliedCourseIds, resumeMappings,
                            availableCourses);
                    if (user instanceof TA ta) {
                        taUsers.add(ta);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return taUsers;
    }

    private static User buildUser(String name, String role, String password, String email, String college, String skill,
            String ownedCourseIds, String appliedCourseIds, String resumeMappings,
            List<Course> availableCourses) {
        User user = null;
        if ("Admin".equals(role)) {
            user = new Admin(password, email);
        }
        if ("TA".equals(role)) {
            user = new TA(password, email);
        }
        if ("Mo".equals(role)) {
            user = new Mo(password, email);
        }
        if (user != null && name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (user instanceof TA ta) {
            ta.setCollege(college);
            ta.setSkill(skill);
        }
        if (user instanceof Mo mo && ownedCourseIds != null && !ownedCourseIds.isBlank()) {
            mo.setOwnedCourses(resolveCourses(ownedCourseIds, availableCourses));
        }
        if (user instanceof TA ta && appliedCourseIds != null && !appliedCourseIds.isBlank()) {
            ta.setAppliedClasses(resolveCourses(appliedCourseIds, availableCourses));
        }
        if (user instanceof TA ta && resumeMappings != null && !resumeMappings.isBlank()) {
            ta.setResumeSubmissions(resolveResumeSubmissions(resumeMappings, availableCourses));
        }
        return user;
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

    private static void updateTaLine(TA ta) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            saveUser(ta);
            return;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4 && "TA".equals(parts[2]) && parts[3].equals(ta.getEmail())) {
                    updatedLines.add(toLine(ta));
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
            updatedLines.add(toLine(ta));
        }

        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toLine(User user) {
        String baseLine = safe(user.getName()) + "," + safe(user.getPassword()) + "," + safe(user.getRole()) + "," + safe(user.getEmail());
        if (user instanceof TA ta) {
            return baseLine + "," + safe(ta.getCollege()) + "," + safe(ta.getSkill()) + ","
                    + serializeAppliedCourseIds(ta) + "," + serializeResumeSubmissions(ta);
        }
        if (user instanceof Mo mo) {
            return baseLine + "," + serializeOwnedCourseIds(mo);
        }
        return baseLine;
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
                .map(submission -> submission.getCourse().getId() + "@" + submission.getResumeDirectory())
                .collect(Collectors.joining("|"));
    }

    private static List<Course> resolveCourses(String appliedCourseIds, List<Course> availableCourses) {
        List<Course> resolvedCourses = new ArrayList<>();
        for (String courseId : appliedCourseIds.split("\\|")) {
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

            int separatorIndex = mapping.indexOf('@');
            if (separatorIndex <= 0 || separatorIndex == mapping.length() - 1) {
                continue;
            }

            String courseId = mapping.substring(0, separatorIndex);
            String resumeDirectory = mapping.substring(separatorIndex + 1);

            for (Course course : availableCourses) {
                if (courseId.equals(course.getId())) {
                    submissions.add(new ResumeSubmission(course, resumeDirectory));
                    break;
                }
            }
        }
        return submissions;
    }

    private static String extractCollege(String role, String[] parts) {
        if (!"TA".equals(role) || parts.length < 8) {
            return "";
        }
        return parts[4];
    }

    private static String extractSkill(String role, String[] parts) {
        if (!"TA".equals(role) || parts.length < 8) {
            return "";
        }
        return parts[5];
    }

    private static String extractOwnedCourseIds(String role, String[] parts) {
        if (!"Mo".equals(role)) {
            return "";
        }
        return parts.length >= 5 ? parts[4] : "";
    }

    private static String extractAppliedCourseIds(String role, String[] parts) {
        if (!"TA".equals(role)) {
            return "";
        }
        if (parts.length >= 8) {
            return parts[6];
        }
        return parts.length >= 5 ? parts[4] : "";
    }

    private static String extractResumeMappings(String role, String[] parts) {
        if (!"TA".equals(role)) {
            return "";
        }
        if (parts.length >= 8) {
            return parts[7];
        }
        return parts.length >= 6 ? parts[5] : "";
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ");
    }
}

