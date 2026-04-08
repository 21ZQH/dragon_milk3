package store;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import model.Course;

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
                String[] parts = line.split(",", 6);
                if (parts.length == 6) {
                    String courseName = parts[0];
                    String jobTitle = parts[1];
                    String workingHours = parts[2];
                    String salary = parts[3];
                    String jobDescription = parts[4];
                    String jobRequirement = parts[5];
                    courseList.add(new Course(courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static String buildCourseLine(Course course) {
        return safe(course.getCourseName()) + ","
                + safe(course.getJobTitle()) + ","
                + safe(course.getWorkingHours()) + ","
                + safe(course.getSalary()) + ","
                + safe(course.getJobDescription()) + ","
                + safe(course.getJobRequirement());
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
}
