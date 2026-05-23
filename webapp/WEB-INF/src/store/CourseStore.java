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

/**
 * Data access layer for persisting and retrieving course records.
 *
 * <p>Courses are stored as CSV records in a file-based store. This class provides
 * operations for reading, saving, updating, and resetting course data used in the
 * TA recruitment workflow. It also manages the association between courses and
 * their TA applicants.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class CourseStore {
    /** System property key for overriding the default course store file path. */
    public static final String FILE_PATH_PROPERTY = "course.store.path";

    /**
     * Retrieves all course records from the store.
     * <p>Supports multiple CSV format versions for backward compatibility,
     * including legacy 6-field records. Applicant data is populated from the
     * {@link UserStore} after all courses are loaded.</p>
     *
     * @return a list of all courses with populated applicant data
     */
    public static List<Course> getCourseList() {
        List<Course> courseList = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return courseList;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = CsvRecord.parse(line);
                if (parts.length >= 10) {
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
                    course.setRecruitmentPublished(Boolean.parseBoolean(parts[9]));
                    if (parts.length >= 11) {
                        course.setTaPositions(parseTaPositions(parts[10]));
                    }
                    courseList.add(course);
                } else if (parts.length >= 9) {
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
                    course.setRecruitmentPublished(false);
                    courseList.add(course);
                } else if (parts.length == 7) {
                    String courseId = parts[0];
                    String courseName = parts[1];
                    String jobTitle = parts[2];
                    String workingHours = parts[3];
                    String salary = parts[4];
                    String jobDescription = parts[5];
                    String jobRequirement = parts[6];
                    Course course = new Course(courseId, courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
                    course.setRecruitmentPublished(false);
                    courseList.add(course);
                } else if (parts.length == 6) {
                    String courseId = buildLegacyCourseId(line);
                    String courseName = parts[0];
                    String jobTitle = parts[1];
                    String workingHours = parts[2];
                    String salary = parts[3];
                    String jobDescription = parts[4];
                    String jobRequirement = parts[5];
                    Course course = new Course(courseId, courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
                    course.setRecruitmentPublished(false);
                    courseList.add(course);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        populateApplicants(courseList);
        return courseList;
    }

    /**
     * Saves a new course record to the store file.
     *
     * @param course the course to save
     */
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

    /**
     * Updates a course at the specified index in the course list.
     *
     * @param courseIndex    the index of the course to update
     * @param updatedCourse  the updated course data
     */
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

    /**
     * Updates a course identified by its unique ID in the store.
     *
     * @param updatedCourse the course with updated data (must have a valid non-blank ID)
     */
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

    /**
     * Resets the recruitment cycle state for all courses.
     * <p>Clears picked applicant emails, review published status, and recruitment
     * published status for every course.</p>
     */
    public static void resetRecruitmentCycle() {
        List<Course> courseList = getCourseList();
        List<String> linesToWrite = new ArrayList<>();
        for (Course course : courseList) {
            course.setPickedApplicantEmails(List.of());
            course.setReviewPublished(false);
            course.setRecruitmentPublished(false);
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

    /**
     * Builds a CSV line representation of a course.
     *
     * @param course the course to serialize
     * @return the CSV line string
     */
    private static String buildCourseLine(Course course) {
        return CsvRecord.toLine(
                safe(course.getId()),
                safe(course.getCourseName()),
                safe(course.getJobTitle()),
                safe(course.getWorkingHours()),
                safe(course.getSalary()),
                safe(course.getJobDescription()),
                safe(course.getJobRequirement()),
                safe(serializePickedApplicantEmails(course)),
                String.valueOf(course.isReviewPublished()),
                String.valueOf(course.isRecruitmentPublished()),
                String.valueOf(course.getTaPositions()));
    }

    /**
     * Returns the given value or an empty string if it is {@code null}.
     *
     * @param value the value to check
     * @return the original value if non-null, or an empty string otherwise
     */
    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Resolves the course store file path from system properties or defaults.
     *
     * @return the resolved file path
     */
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

    /**
     * Ensures the parent directory of the given file path exists, creating it if necessary.
     *
     * @param filePath the file path whose parent directory should be checked
     * @throws IOException if an I/O error occurs during directory creation
     */
    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }

    /**
     * Builds a legacy course identifier from the CSV line content.
     * <p>Used for backward compatibility with older 6-field course records.</p>
     *
     * @param line the original CSV line
     * @return a UUID-based legacy course identifier
     */
    private static String buildLegacyCourseId(String line) {
        return "legacy-" + UUID.nameUUIDFromBytes(line.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Serializes the picked applicant emails of a course into a pipe-delimited string.
     *
     * @param course the course whose picked applicant emails to serialize
     * @return the serialized email string
     */
    private static String serializePickedApplicantEmails(Course course) {
        return course.getPickedApplicantEmails().stream()
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .collect(Collectors.joining("|"));
    }

    /**
     * Parses a pipe-delimited string of email addresses into a list.
     *
     * @param value the pipe-delimited email string to parse
     * @return the list of parsed email addresses
     */
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

    /**
     * Parses the configured number of TA positions.
     *
     * @param value the raw stored value
     * @return a positive number of TA positions, or {@code 0} if missing/invalid
     */
    private static int parseTaPositions(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Populates the applicant lists for all courses from the TA user store.
     *
     * @param courseList the list of courses to populate with applicants
     */
    private static void populateApplicants(List<Course> courseList) {
        if (courseList.isEmpty()) {
            return;
        }

        List<TA> taUsers = UserStore.getTaUsersForCourses(courseList);
        for (TA ta : taUsers) {
            for (Course appliedCourse : ta.getAppliedClasses()) {
                String applicationFormId = ta.getApplicationFormIdForCourse(appliedCourse.getId());
                appliedCourse.addApplication(ta, applicationFormId);
            }
        }
    }
}
