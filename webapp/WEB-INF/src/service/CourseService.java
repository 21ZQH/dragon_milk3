package service;

import java.util.List;

import model.Course;

public interface CourseService {
    List<Course> getCourseList();

    void saveCourse(Course course);

    void updateCourse(Course course);

    void resetRecruitmentCycle();
}
