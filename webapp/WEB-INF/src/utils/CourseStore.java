package utils;


import model.Course;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CourseStore {
    private static final String FILE_PATH = "G:\\Tomcat\\webapps\\SE\\WEB-INF\\file\\courses.txt";

    public static List<Course> getCourseList() {
        List<Course> courseList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // courseName, jobTitle, workingHours, salary
                if (parts.length == 4) {
                    String courseName = parts[0];
                    String jobTitle = parts[1];
                    String workingHours = parts[2];
                    String salary = parts[3];
                    courseList.add(new Course(courseName, jobTitle, workingHours, salary));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courseList;
    }
}
