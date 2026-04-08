package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import model.Course;
import model.TA;
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

    @Test
    void loadCourseListPopulatesApplicantsFromUserStore() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Java,TA,8 hours/week,TBD,Mark assignments,Java basics");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@D:\\resume\\course-1");

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        Course course = courses.get(0);
        assertEquals(1, course.getTaApplicants().size());
        assertEquals(1, course.getApplicantResumes().size());
        assertEquals("D:\\resume\\course-1", course.getApplicantResumes().get(0));

        TA applicant = course.getTaApplicants().get(0);
        assertEquals("alice@example.com", applicant.getEmail());
        assertEquals("Alice", applicant.getName());
        assertEquals("School of Software", applicant.getCollege());
        assertEquals("Java", applicant.getSkill());
        assertEquals(1, applicant.getAppliedClasses().size());
        assertEquals("course-1", applicant.getAppliedClasses().get(0).getId());
        assertNotNull(applicant.getResumeDirectoryForCourse("course-1"));
        assertEquals("D:\\resume\\course-1", applicant.getResumeDirectoryForCourse("course-1"));
    }
}
