package service.impl;

import java.util.List;

import model.Course;
import service.CourseService;
import store.CourseStore;

public class CourseServiceImpl implements CourseService {
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
