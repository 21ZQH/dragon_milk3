package service;

import java.util.List;

import model.Course;

/**
 * Service interface for managing courses within the TA recruitment system.
 * Provides methods for retrieving, saving, updating courses, and resetting
 * course data as part of the recruitment cycle management.
 *
 * @version 1.0
 * @since 2025
 */
public interface CourseService {
    /**
     * Retrieves a list of all courses in the system.
     *
     * @return a list of {@link Course} objects
     */
    List<Course> getCourseList();

    /**
     * Saves a new course to the data store.
     *
     * @param course the {@link Course} object to persist
     */
    void saveCourse(Course course);

    /**
     * Updates an existing course with new data.
     *
     * @param course the {@link Course} object containing updated information
     */
    void updateCourse(Course course);

    /**
     * Resets all courses to their initial draft state as part of
     * a recruitment cycle reset, clearing application and review data.
     */
    void resetRecruitmentCycle();
}
