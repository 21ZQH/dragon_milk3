package service.impl;

import java.util.List;

import model.Course;
import repository.CourseRepository;
import repository.impl.TxtCourseRepositoryImpl;
import service.CourseService;

/**
 * Implementation of the {@link CourseService} interface.
 * Provides concrete operations for managing courses, including
 * retrieving the full course list, saving, updating, and resetting
 * the recruitment cycle. Delegates all data access to
 * {@link CourseRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class CourseServiceImpl implements CourseService {
    /** Repository for course persistence operations. */
    private final CourseRepository courseRepository;

    /**
     * Constructs a new {@code CourseServiceImpl} with a default
     * {@link TxtCourseRepositoryImpl}.
     */
    public CourseServiceImpl() {
        this(new TxtCourseRepositoryImpl());
    }

    /**
     * Constructs a new {@code CourseServiceImpl} with the given repository.
     *
     * @param courseRepository the repository for course data access
     */
    CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Retrieves the list of all courses.
     *
     * @return a list of {@link Course} objects
     */
    @Override
    public List<Course> getCourseList() {
        return courseRepository.getCourseList();
    }

    /**
     * Persists a new course to the data store.
     *
     * @param course the course to save
     */
    @Override
    public void saveCourse(Course course) {
        courseRepository.saveCourse(course);
    }

    /**
     * Updates an existing course in the data store.
     *
     * @param course the course with updated data
     */
    @Override
    public void updateCourse(Course course) {
        courseRepository.updateCourse(course);
    }

    /**
     * Resets the recruitment cycle for all courses, clearing any published
     * recruitment statuses and applicant data.
     */
    @Override
    public void resetRecruitmentCycle() {
        courseRepository.resetRecruitmentCycle();
    }
}
