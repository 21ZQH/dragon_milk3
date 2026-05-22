package repository;

import java.util.List;

import model.Course;

/**
 * Repository boundary for course and recruitment-project data.
 *
 * <p>Services depend on this interface so course persistence can remain hidden
 * behind a stable contract. The current implementation stores records in
 * {@code WEB-INF/file/courses.txt}.</p>
 */
public interface CourseRepository {
    /**
     * Loads every course known to the system, including draft and published
     * recruitment projects.
     *
     * @return mutable list of course models; empty when no course file exists
     */
    List<Course> getCourseList();

    /**
     * Appends or persists a course record.
     *
     * @param course course to save
     */
    void saveCourse(Course course);

    /**
     * Replaces an existing course record matched by course id.
     *
     * @param course updated course state
     */
    void updateCourse(Course course);

    void resetRecruitmentCycle();
}
