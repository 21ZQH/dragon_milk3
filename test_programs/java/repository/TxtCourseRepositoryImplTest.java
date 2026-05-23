package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.impl.TxtCourseRepositoryImpl;
import testsupport.StoreTestSupport;

/**
 * Unit tests for the {@link TxtCourseRepositoryImpl} class in the TA Recruitment system.
 * Verifies that course persistence operations are correctly delegated to the underlying
 * text-based store and that course data is properly saved and retrieved.
 *
 * @author TA Recruitment System
 */
class TxtCourseRepositoryImplTest {
    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * The repository instance under test.
     */
    private final CourseRepository repository = new TxtCourseRepositoryImpl();

    /**
     * Cleans up store system property overrides after each test.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Verifies that saving a course and retrieving the course list delegates to the
     * underlying TxtCourseStore, and that the persisted course data is stored and
     * retrieved correctly.
     */
    @Test
    void delegatesCoursePersistenceToTxtStore() {
        StoreTestSupport.useCourseStore(tempDir);

        repository.saveCourse(new Course("course-1", "Software Engineering", "TA",
                "10 hours/week", "TBD", "Support labs", "Java"));

        List<Course> courses = repository.getCourseList();
        assertEquals(1, courses.size());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
    }
}
