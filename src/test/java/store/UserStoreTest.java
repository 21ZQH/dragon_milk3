package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StoreTestSupport;

/**
 * Unit tests for the {@link UserStore} class in the TA Recruitment system.
 * Verifies that user persistence operations, including saving, validation,
 * profile updates, and restoration of associated data (owned courses, applied
 * courses, resume submissions, review flags), function correctly across
 * different user roles and file formats.
 *
 * @author TA Recruitment System
 */
class UserStoreTest {
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
     * Verifies that saving a user and validating by role uses the isolated test file,
     * and that the persisted user data is written to the file system and can be
     * retrieved with all fields correctly populated.
     */
    @Test
    void saveUserAndValidateByRoleUsesIsolatedTestFile() throws Exception {
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        Mo user = new Mo("secret123", "mo@example.com");
        user.setName("Molly");

        UserStore.saveUser(user);

        assertTrue(Files.exists(usersFile));

        List<String> lines = Files.readAllLines(usersFile);
        assertEquals(1, lines.size());
        assertEquals("Molly,secret123,Mo,mo@example.com,", lines.get(0));

        User savedUser = UserStore.validateUser("secret123", "Mo", "mo@example.com");
        assertNotNull(savedUser);
        assertEquals("Mo", savedUser.getRole());
        assertEquals("mo@example.com", savedUser.getEmail());
        assertEquals("Molly", savedUser.getName());
        assertEquals(0, ((Mo) savedUser).getOwnedCourses().size());
    }

    /**
     * Verifies that validating an MO user restores their owned courses by resolving
     * course identifiers from the associated course store.
     */
    @Test
    void validateUserRestoresOwnedCoursesForMo() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills",
                "course-2,Java,TA,8 hours/week,TBD,Mark assignments,Java basics");
        StoreTestSupport.writeLines(
                usersFile,
                "Molly,secret123,Mo,mo@example.com,course-1|course-2");

        Mo mo = (Mo) UserStore.validateUser("secret123", "Mo", "mo@example.com");

        assertEquals("Molly", mo.getName());
        assertEquals(2, mo.getOwnedCourses().size());
        assertEquals("course-1", mo.getOwnedCourses().get(0).getId());
        assertEquals("course-2", mo.getOwnedCourses().get(1).getId());
    }

    /**
     * Verifies that validating an MO user restores profile fields such as degree
     * and college from the new user file format, along with owned course references.
     */
    @Test
    void validateUserRestoresMoProfileFieldsFromNewFormat() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Molly,secret123,Mo,mo@example.com,Master of Science,School of Software,course-1");

        Mo mo = (Mo) UserStore.validateUser("secret123", "Mo", "mo@example.com");

        assertNotNull(mo);
        assertEquals("Molly", mo.getName());
        assertEquals("Master of Science", mo.getDegree());
        assertEquals("School of Software", mo.getCollege());
        assertEquals(1, mo.getOwnedCourses().size());
        assertEquals("course-1", mo.getOwnedCourses().get(0).getId());
    }

    /**
     * Verifies that validating a user without specifying a role correctly matches
     * by email and password, and that applied course references are restored for
     * the TA user.
     */
    @Test
    void validateUserWithoutRoleMatchesEmailAndPassword() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com,course-1");

        User user = UserStore.validateUser("pass123", "alice@example.com");

        assertNotNull(user);
        assertEquals("TA", user.getRole());
        assertEquals("Alice", user.getName());
        TA ta = (TA) user;
        assertEquals("", ta.getCollege());
        assertEquals("", ta.getSkill());
        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals("course-1", ta.getAppliedClasses().get(0).getId());
        assertEquals(0, ta.getResumeSubmissions().size());
    }

    /**
     * Verifies that updating applied course IDs correctly rewrites the TA user line
     * with the course identifiers appended to the CSV record.
     */
    @Test
    void updateAppliedCourseIdsRewritesTaLineWithCourseIds() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com");

        TA ta = new TA("pass123", "alice@example.com");
        ta.setName("Alice");
        ta.setCollege("School of Software");
        ta.setSkill("Java");
        ta.addClass(new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills"));

        UserStore.updateAppliedCourseIds(ta);

        List<String> lines = Files.readAllLines(usersFile);
        assertEquals("Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,", lines.get(0));
    }

    /**
     * Verifies that validating a TA user restores application form submissions
     * with their correct status and review-read flag for matching courses.
     */
    @Test
    void validateUserRestoresApplicationFormSubmissionForMatchingCourse() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(usersFile, "Alice,pass123,TA,alice@example.com,course-1,course-1@0@false");

        TA ta = (TA) UserStore.validateUser("pass123", "alice@example.com");

        assertEquals(1, ta.getResumeSubmissions().size());
        ResumeSubmission submission = ta.getResumeSubmissions().get(0);
        assertEquals("course-1", submission.getCourse().getId());
        assertEquals("course-1", submission.getApplicationFormId());
        assertEquals(ResumeSubmission.STATUS_PENDING, submission.getStatus());
        assertFalse(submission.isReviewUnread());
    }

    /**
     * Verifies that validating a TA user restores profile fields (college, skill)
     * and application form metadata from the new user file format.
     */
    @Test
    void validateUserRestoresTaProfileFieldsFromNewFormat() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@2@false");

        TA ta = (TA) UserStore.validateUser("pass123", "alice@example.com");

        assertEquals("Alice", ta.getName());
        assertEquals("School of Software", ta.getCollege());
        assertEquals("Java", ta.getSkill());
        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals("course-1", ta.getAppliedClasses().get(0).getId());
        assertEquals("course-1", ta.getApplicationFormIdForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_REJECTED, ta.getResumeStatusForCourse("course-1"));
        assertFalse(ta.hasUnreadReviewUpdates());
    }

    /**
     * Verifies that updating a TA profile rewrites the profile fields (name, college,
     * skill) while preserving existing course and application form mappings in the
     * CSV record.
     */
    @Test
    void updateTaProfileRewritesOnlyTaProfileFieldsAndPreservesMappings() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false");

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("pass123", "alice@example.com");
        ta.setName("Alice Zhang");
        ta.setCollege("New College");
        ta.setSkill("Java, Python, SQL");
        ta.addClass(course);
        ta.addOrUpdateApplication(course, course.getId());

        UserStore.updateTaProfile(ta);

        List<String> lines = Files.readAllLines(usersFile);
        assertEquals("Alice Zhang,pass123,TA,alice@example.com,New College,\"Java, Python, SQL\",course-1,course-1@0@false", lines.get(0));
    }

    /**
     * Verifies that updating a TA profile correctly handles fields containing commas
     * and double quotes in CSV serialization and deserialization, preserving the
     * original special characters in the round-trip.
     */
    @Test
    void updateTaProfilePreservesCommasAndQuotesInCsvFields() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@0@false");

        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA ta = new TA("pass123", "alice@example.com");
        ta.setName("Alice, Zhang");
        ta.setCollege("School of \"Software\", BUPT");
        ta.setSkill("Java, Python, SQL");
        ta.addClass(course);
        ta.addOrUpdateApplication(course, course.getId());

        UserStore.updateTaProfile(ta);

        String line = Files.readAllLines(usersFile).get(0);
        assertEquals("\"Alice, Zhang\",pass123,TA,alice@example.com,\"School of \"\"Software\"\", BUPT\",\"Java, Python, SQL\",course-1,course-1@0@false", line);

        TA savedTa = (TA) UserStore.validateUser("pass123", "alice@example.com");
        assertEquals("Alice, Zhang", savedTa.getName());
        assertEquals("School of \"Software\", BUPT", savedTa.getCollege());
        assertEquals("Java, Python, SQL", savedTa.getSkill());
        assertEquals("course-1", savedTa.getApplicationFormIdForCourse("course-1"));
    }

    /**
     * Verifies that validating a TA user restores the unread review flag and review
     * status from the new user file format, correctly identifying approved submissions
     * with unread updates.
     */
    @Test
    void validateUserRestoresUnreadReviewFlagFromNewFormat() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        Path usersFile = StoreTestSupport.useUserStore(tempDir);
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
        StoreTestSupport.writeLines(
                usersFile,
                "Alice,pass123,TA,alice@example.com,School of Software,Java,course-1,course-1@1@true");

        TA ta = (TA) UserStore.validateUser("pass123", "alice@example.com");

        assertTrue(ta.hasUnreadReviewUpdates());
        assertTrue(ta.isReviewUnreadForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_APPROVED, ta.getResumeStatusForCourse("course-1"));
    }

    /**
     * Verifies that when no user file exists, the store behaves as if no users are
     * registered, returning false for email existence checks and null for all user
     * validation methods.
     */
    @Test
    void missingUserFileBehavesLikeNoRegisteredUsers() {
        StoreTestSupport.useUserStore(tempDir);

        assertFalse(UserStore.isEmailRegistered("nobody@example.com"));
        assertNull(UserStore.validateUser("secret", "TA", "nobody@example.com"));
        assertNull(UserStore.validateUser("secret", "nobody@example.com"));
    }
}
