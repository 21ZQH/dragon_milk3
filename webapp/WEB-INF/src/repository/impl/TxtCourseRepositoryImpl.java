package repository.impl;

import java.util.List;

import model.Course;
import repository.CourseRepository;
import store.CourseStore;

/**
 * Text-file implementation of {@link CourseRepository}.
 *
 * <p>The class keeps repository-to-store calls explicit and simple. CSV
 * parsing, escaping, and file-path resolution are handled inside
 * {@link CourseStore}.</p>
 */
public class TxtCourseRepositoryImpl implements CourseRepository {
    /**
     * Loads all courses from {@link CourseStore}.
     *
     * @return list of courses from the text store
     */
    @Override
    public List<Course> getCourseList() {
        return CourseStore.getCourseList();
    }

    /**
     * Saves a course through {@link CourseStore#saveCourse(Course)}.
     *
     * @param course course to persist
     */
    @Override
    public void saveCourse(Course course) {
        CourseStore.saveCourse(course);
    }

    /**
     * Updates a course through {@link CourseStore#updateCourse(Course)}.
     *
     * @param course updated course
     */
    @Override
    public void updateCourse(Course course) {
        CourseStore.updateCourse(course);
    }

    @Override
    public void resetRecruitmentCycle() {
        CourseStore.resetRecruitmentCycle();
    }
}
