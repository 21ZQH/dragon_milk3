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

/**
 * Unit tests for the {@link CourseStore} class in the TA Recruitment system.
 * Verifies that course persistence operations, including saving, loading, CSV
 * serialization, metadata restoration, and error handling, function correctly.
 *
 * @author TA Recruitment System
 */
class CourseStoreTest {
    /**
     * Temporary directory for isolated test file storage.
     */
    @TempDir
    Path tempDir;

    /**
     * Cleans up store system property overrides after each test.
     */
    @AfterEach
    void tearDown() {
        StoreTestSupport.clearStoreOverrides();
    }

    /**
     * Verifies that a saved course can be retrieved from the course list with all
     * its fields correctly populated, including course name, job title, and job
     * description.
     */
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
        assertEquals(0, courses.get(0).getTaPositions());
    }

    /**
     * Verifies that course fields containing commas and double quotes are properly
     * escaped in CSV output and correctly deserialized upon loading, preserving
     * the original field values.
     */
    @Test
    void saveCoursePreservesCommasAndQuotesInCsvFields() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);

        CourseStore.saveCourse(new Course(
                "Data, AI and Society",
                "TA \"Lead\"",
                "8 hours/week",
                "TBD",
                "Support labs, tutorials, and grading",
                "Know Python, SQL, and \"data ethics\""));

        String rawLine = java.nio.file.Files.readAllLines(courseFile).get(0);
        assertTrue(rawLine.contains("\"Data, AI and Society\""));
        assertTrue(rawLine.contains("\"TA \"\"Lead\"\"\""));

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        assertEquals("Data, AI and Society", courses.get(0).getCourseName());
        assertEquals("TA \"Lead\"", courses.get(0).getJobTitle());
        assertEquals("Support labs, tutorials, and grading", courses.get(0).getJobDescription());
        assertEquals("Know Python, SQL, and \"data ethics\"", courses.get(0).getJobRequirement());
    }

    /**
     * Verifies that malformed CSV rows with an incorrect number of fields are
     * silently ignored when loading courses, and only valid rows are included
     * in the returned course list.
     */
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

    /**
     * Verifies that an empty course list is returned when no course file exists
     * in the configured store path.
     */
    @Test
    void missingCourseFileReturnsEmptyList() {
        StoreTestSupport.useCourseStore(tempDir);

        assertTrue(CourseStore.getCourseList().isEmpty());
    }

    /**
     * Verifies that loading the course list correctly populates TA applicants and
     * their associated application form IDs by reading from the linked user store.
     */
    @Test
    void loadCourseListPopulatesApplicantsFromUserStore() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Java,TA,8 hours/week,TBD,Mark assignments,Java basics");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false");

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        Course course = courses.get(0);
        assertEquals(1, course.getTaApplicants().size());
        assertEquals(1, course.getApplicantFormIds().size());
        assertEquals("course-1", course.getApplicantFormIds().get(0));

        TA applicant = course.getTaApplicants().get(0);
        assertEquals("alice@example.com", applicant.getEmail());
        assertEquals("Alice", applicant.getName());
        assertEquals("School of Software", applicant.getCollege());
        assertEquals("Java", applicant.getSkill());
        assertEquals(1, applicant.getAppliedClasses().size());
        assertEquals("course-1", applicant.getAppliedClasses().get(0).getId());
        assertNotNull(applicant.getApplicationFormIdForCourse("course-1"));
        assertEquals("course-1", applicant.getApplicationFormIdForCourse("course-1"));
    }

    /**
     * Verifies that loading the course list correctly restores review publication
     * status and picked applicant email metadata from extended CSV fields.
     */
    @Test
    void loadCourseListRestoresReviewSelectionMetadata() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Java,TA,8 hours/week,TBD,Mark assignments,Java basics,alice@example.com|bob@example.com,true");

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        Course course = courses.get(0);
        assertTrue(course.isReviewPublished());
        assertEquals(2, course.getPickedApplicantEmails().size());
        assertTrue(course.isApplicantPicked("alice@example.com"));
        assertTrue(course.isApplicantPicked("bob@example.com"));
    }

    /**
     * Verifies that the TA position quota is restored from the extended CSV field.
     */
    @Test
    void loadCourseListRestoresTaPositions() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Java,TA,8 hours/week,TBD,Mark assignments,Java basics,,false,true,2");

        List<Course> courses = CourseStore.getCourseList();

        assertEquals(1, courses.size());
        assertEquals(2, courses.get(0).getTaPositions());
    }
}
