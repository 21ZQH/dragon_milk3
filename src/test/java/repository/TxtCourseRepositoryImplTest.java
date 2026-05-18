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

class TxtCourseRepositoryImplTest {
    @TempDir
    Path tempDir;

    private final CourseRepository repository = new TxtCourseRepositoryImpl();

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

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
