package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.Course;

public class CourseStore {
    private static final String FILE_PATH = "C:\\Tomcat11\\webapps\\webapp\\WEB-INF\\file\\courses.txt";

    public static List<Course> getCourseList() {
        List<Course> courseList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // 加了两个新字段，这里判断长度改为 6
                if (parts.length == 6) {
                    String courseName = parts[0];
                    String jobTitle = parts[1];
                    String workingHours = parts[2];
                    String salary = parts[3];
                    String jobDescription = parts[4];
                    String jobRequirement = parts[5];
                    // 更新构造函数的调用（因为course那个类进行了修改）
                    courseList.add(new Course(courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courseList;
    }

    // 新增专门用于保存 Course 的方法
    public static void saveCourse(Course course) {
        String line = course.getCourseName() + "," + 
                      course.getJobTitle() + "," + 
                      course.getWorkingHours() + "," + 
                      course.getSalary() + "," + 
                      course.getJobDescription() + "," + 
                      course.getJobRequirement();
                      
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) { 
            fw.write(line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}