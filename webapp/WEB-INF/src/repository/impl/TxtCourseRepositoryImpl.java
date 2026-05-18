package repository.impl;

import java.util.List;

import model.Course;
import repository.CourseRepository;
import store.CourseStore;

public class TxtCourseRepositoryImpl implements CourseRepository {
    @Override
    public List<Course> getCourseList() {
        return CourseStore.getCourseList();
    }

    @Override
    public void saveCourse(Course course) {
        CourseStore.saveCourse(course);
    }

    @Override
    public void updateCourse(Course course) {
        CourseStore.updateCourse(course);
    }
}
