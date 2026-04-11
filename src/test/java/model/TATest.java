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
    void addOrUpdateResumeReplacesExistingResumeForSameCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateResume(course, "D:\\resume\\old");
        ta.addOrUpdateResume(course, "D:\\resume\\new");

        assertEquals(1, ta.getAppliedClasses().size());
        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals("D:\\resume\\new", ta.getResumeDirectoryForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_PENDING, ta.getResumeStatusForCourse("course-1"));
    }

    @Test
    void addOrUpdateResumeCanReplaceStatusForSameCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateResume(course, "D:\\resume\\old", ResumeSubmission.STATUS_PENDING);
        ta.addOrUpdateResume(course, "D:\\resume\\old", ResumeSubmission.STATUS_APPROVED);

        assertEquals(1, ta.getResumeSubmissions().size());
        assertEquals(ResumeSubmission.STATUS_APPROVED, ta.getResumeStatusForCourse("course-1"));
    }

    @Test
    void markAllReviewUpdatesReadClearsUnreadFlags() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateResume(course, "D:\\resume\\course-1", ResumeSubmission.STATUS_APPROVED, true);

        assertTrue(ta.hasUnreadReviewUpdates());
        assertTrue(ta.isReviewUnreadForCourse("course-1"));
        assertTrue(ta.markAllReviewUpdatesRead());
        assertFalse(ta.hasUnreadReviewUpdates());
        assertFalse(ta.isReviewUnreadForCourse("course-1"));
    }

    @Test
    void withdrawApplicationRemovesCourseAndResumeSubmission() {
        TA ta = new TA("secret123", "ta@example.com");
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");

        ta.addOrUpdateResume(course, "D:\\resume\\course-1");
        ta.withdrawApplication("course-1");

        assertEquals(0, ta.getAppliedClasses().size());
        assertEquals(0, ta.getResumeSubmissions().size());
        assertNull(ta.getResumeDirectoryForCourse("course-1"));
    }
}
