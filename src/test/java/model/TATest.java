package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TATest {
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

    @Test
    void addOrUpdateApplicationCanReplaceStatusForSameCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateApplication(course, "course-1", ResumeSubmission.STATUS_PENDING);
        ta.addOrUpdateApplication(course, "course-1", ResumeSubmission.STATUS_APPROVED);

        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals(ResumeSubmission.STATUS_APPROVED, ta.getResumeStatusForCourse("course-1"));
    }

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
