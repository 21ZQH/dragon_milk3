package store;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import model.Course;
import model.TA;

public class CourseStore {
    public static final String FILE_PATH_PROPERTY = "course.store.path";

    public static List<Course> getCourseList() {
        List<Course> courseList = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return courseList;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    String courseId = parts[0];
                    String courseName = parts[1];
                    String jobTitle = parts[2];
                    String workingHours = parts[3];
                    String salary = parts[4];
                    String jobDescription = parts[5];
                    String jobRequirement = parts[6];
                    Course course = new Course(courseId, courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
                    course.setPickedApplicantEmails(parsePickedApplicantEmails(parts[7]));
                    course.setReviewPublished(Boolean.parseBoolean(parts[8]));
                    courseList.add(course);
                } else if (parts.length == 7) {
                    String courseId = parts[0];
                    String courseName = parts[1];
                    String jobTitle = parts[2];
                    String workingHours = parts[3];
                    String salary = parts[4];
                    String jobDescription = parts[5];
                    String jobRequirement = parts[6];
                    courseList.add(new Course(courseId, courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement));
                } else if (parts.length == 6) {
                    String courseId = buildLegacyCourseId(line);
                    String courseName = parts[0];
                    String jobTitle = parts[1];
                    String workingHours = parts[2];
                    String salary = parts[3];
                    String jobDescription = parts[4];
                    String jobRequirement = parts[5];
                    courseList.add(new Course(courseId, courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        populateApplicants(courseList);
        return courseList;
    }

    public static void saveCourse(Course course) {
        String line = buildCourseLine(course);

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

    public static void updateCourse(int courseIndex, Course updatedCourse) {
        List<Course> courseList = getCourseList();
        if (courseIndex < 0 || courseIndex >= courseList.size()) {
            return;
        }

        courseList.set(courseIndex, updatedCourse);

        List<String> linesToWrite = new ArrayList<>();
        for (Course course : courseList) {
            linesToWrite.add(buildCourseLine(course));
        }

        Path filePath = resolveFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, linesToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateCourse(Course updatedCourse) {
        if (updatedCourse == null || updatedCourse.getId() == null || updatedCourse.getId().isBlank()) {
            return;
        }

        List<Course> courseList = getCourseList();
        for (int i = 0; i < courseList.size(); i++) {
            if (updatedCourse.getId().equals(courseList.get(i).getId())) {
                courseList.set(i, updatedCourse);

                List<String> linesToWrite = new ArrayList<>();
                for (Course course : courseList) {
                    linesToWrite.add(buildCourseLine(course));
                }

                Path filePath = resolveFilePath();
                try {
                    ensureParentDirectoryExists(filePath);
                    Files.write(filePath, linesToWrite);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    private static String buildCourseLine(Course course) {
        return safe(course.getId()) + ","
                + safe(course.getCourseName()) + ","
                + safe(course.getJobTitle()) + ","
                + safe(course.getWorkingHours()) + ","
                + safe(course.getSalary()) + ","
                + safe(course.getJobDescription()) + ","
                + safe(course.getJobRequirement()) + ","
                + safe(serializePickedApplicantEmails(course)) + ","
                + course.isReviewPublished();
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ");
    }

    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "courses.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "courses.txt");
    }

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }

    private static String buildLegacyCourseId(String line) {
        return "legacy-" + UUID.nameUUIDFromBytes(line.getBytes(StandardCharsets.UTF_8));
    }

    private static String serializePickedApplicantEmails(Course course) {
        return course.getPickedApplicantEmails().stream()
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .collect(Collectors.joining("|"));
    }

    private static List<String> parsePickedApplicantEmails(String value) {
        List<String> pickedApplicantEmails = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return pickedApplicantEmails;
        }

        for (String email : value.split("\\|")) {
            if (email != null && !email.isBlank() && !pickedApplicantEmails.contains(email)) {
                pickedApplicantEmails.add(email);
            }
        }
        return pickedApplicantEmails;
    }

    private static void populateApplicants(List<Course> courseList) {
        if (courseList.isEmpty()) {
            return;
        }

        List<TA> taUsers = UserStore.getTaUsersForCourses(courseList);
        for (TA ta : taUsers) {
            for (Course appliedCourse : ta.getAppliedClasses()) {
                String resumeDirectory = ta.getResumeDirectoryForCourse(appliedCourse.getId());
                appliedCourse.addApplication(ta, resumeDirectory);
            }
        }
    }
}
