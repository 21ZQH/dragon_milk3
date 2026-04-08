package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StoreTestSupport;

class CourseStoreTest {
    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    @Test
    void saveCourseAndLoadCourseList() {
        StoreTestSupport.useCourseStore(tempDir);

        CourseStore.saveCourse(new Course(
                "Software Engineering",
                "Teaching Assistant",
                "10 hours/week",
                "TBD",
                "Support labs",
                "Strong communication"));

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        assertTrue(courses.get(0).getId() != null && !courses.get(0).getId().isBlank());
        assertEquals("Software Engineering", courses.get(0).getCourseName());
        assertEquals("Teaching Assistant", courses.get(0).getJobTitle());
        assertEquals("Support labs", courses.get(0).getJobDescription());
    }

    @Test
    void malformedRowsAreIgnoredWhenLoadingCourses() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "Bad,row,with,too,few",
                "course-1,Java,TA,8 hours/week,TBD,Mark assignments,Java basics");

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        assertEquals("course-1", courses.get(0).getId());
        assertEquals("Java", courses.get(0).getCourseName());
    }

    @Test
    void missingCourseFileReturnsEmptyList() {
        StoreTestSupport.useCourseStore(tempDir);

        assertTrue(CourseStore.getCourseList().isEmpty());
    }
}
