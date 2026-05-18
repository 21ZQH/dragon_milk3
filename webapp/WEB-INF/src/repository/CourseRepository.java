package repository;

import java.util.List;

import model.Course;

public interface CourseRepository {
    List<Course> getCourseList();

    void saveCourse(Course course);

    void updateCourse(Course course);
}
