package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TA} model class in the TA Recruitment system.
 * Tests cover course management, application handling, resume status updates,
 * review notification flags, and application withdrawal functionality.
 *
 * @author BUPT-TA-Recruitment-Group33
 */
class TATest {
    /**
     * Tests that adding a course with the same ID as an already added course
     * does not create a duplicate entry in the TA's applied classes list.
     */
    @Test
    void addClassDoesNotDuplicateCoursesWithSameId() {
        TA ta = new TA("secret123", "ta@example.com");
        Course originalCourse = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        Course sameCourseDifferentObject = new Course("course-1", "Software Engineering 2", "TA", "12 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addClass(originalCourse);
        ta.addClass(sameCourseDifferentObject);

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals("course-1", ta.getAppliedClasses().get(0).getId());
    }

    /**
     * Tests that adding or updating an application for the same course replaces
     * the existing form ID while keeping only one submission entry.
     */
    @Test
    void addOrUpdateApplicationReplacesExistingFormForSameCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateApplication(course, "course-1-draft");
        ta.addOrUpdateApplication(course, "course-1");

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals("course-1", ta.getApplicationFormIdForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_PENDING, ta.getResumeStatusForCourse("course-1"));
    }

    /**
     * Tests that adding or updating an application can replace the resume status
     * for the same course, such as changing from PENDING to APPROVED.
     */
    @Test
    void addOrUpdateApplicationCanReplaceStatusForSameCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateApplication(course, "course-1", ResumeSubmission.STATUS_PENDING);
        ta.addOrUpdateApplication(course, "course-1", ResumeSubmission.STATUS_APPROVED);

        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals(ResumeSubmission.STATUS_APPROVED, ta.getResumeStatusForCourse("course-1"));
    }

    /**
     * Tests that marking all review updates as read clears the unread flags
     * for all courses and returns true when there were flags to clear.
     */
    @Test
    void markAllReviewUpdatesReadClearsUnreadFlags() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateApplication(course, "course-1", ResumeSubmission.STATUS_APPROVED, true);

        assertTrue(ta.hasUnreadReviewUpdates());
        assertTrue(ta.isReviewUnreadForCourse("course-1"));
        assertTrue(ta.markAllReviewUpdatesRead());
        assertFalse(ta.hasUnreadReviewUpdates());
        assertFalse(ta.isReviewUnreadForCourse("course-1"));
    }

    /**
     * Tests that withdrawing an application removes the course from the applied classes
     * list and clears all associated resume submission data.
     */
    @Test
    void withdrawApplicationRemovesCourseAndApplicationSubmission() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateApplication(course, "course-1");
        ta.withdrawApplication("course-1");

        assertEquals(0, ta.getAppliedClasses().size());
        assertEquals(0, ta.getResumeSubmissions().size());
        assertNull(ta.getApplicationFormIdForCourse("course-1"));
    }
}
