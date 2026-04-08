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

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    String appliedCourseIds = parts.length >= 5 ? parts[4] : "";
                    String resumeMappings = parts.length >= 6 ? parts[5] : "";
                    if (p.equals(password) && r.equals(role) && e.equals(email)) {
                        return buildUser(name, r, p, e, appliedCourseIds, resumeMappings);
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

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    String appliedCourseIds = parts.length >= 5 ? parts[4] : "";
                    String resumeMappings = parts.length >= 6 ? parts[5] : "";
                    if (p.equals(password) && e.equals(email)) {
                        return buildUser(name, r, p, e, appliedCourseIds, resumeMappings);
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

    private static User buildUser(String name, String role, String password, String email, String appliedCourseIds, String resumeMappings) {
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
        if (user instanceof TA ta && appliedCourseIds != null && !appliedCourseIds.isBlank()) {
            ta.setAppliedClasses(resolveCourses(appliedCourseIds));
        }
        if (user instanceof TA ta && resumeMappings != null && !resumeMappings.isBlank()) {
            ta.setResumeSubmissions(resolveResumeSubmissions(resumeMappings));
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

    private static String toLine(User user) {
        String baseLine = user.getName() + "," + user.getPassword() + "," + user.getRole() + "," + user.getEmail();
        if (user instanceof TA ta) {
            return baseLine + "," + serializeAppliedCourseIds(ta) + "," + serializeResumeSubmissions(ta);
        }
        return baseLine;
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

    private static List<Course> resolveCourses(String appliedCourseIds) {
        List<Course> availableCourses = CourseStore.getCourseList();
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

    private static List<ResumeSubmission> resolveResumeSubmissions(String resumeMappings) {
        List<Course> availableCourses = CourseStore.getCourseList();
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
}
