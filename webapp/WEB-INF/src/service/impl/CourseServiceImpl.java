package service.impl;

import java.util.List;

import model.Course;
import repository.CourseRepository;
import repository.impl.TxtCourseRepositoryImpl;
import service.CourseService;

public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    public CourseServiceImpl() {
        this(new TxtCourseRepositoryImpl());
    }

    CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> getCourseList() {
        return courseRepository.getCourseList();
    }

    @Override
    public void saveCourse(Course course) {
        courseRepository.saveCourse(course);
    }

    @Override
    public void updateCourse(Course course) {
        courseRepository.updateCourse(course);
    }
}
