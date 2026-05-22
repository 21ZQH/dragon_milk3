package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.ApplicationForm;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import service.impl.ApplicationReviewServiceImpl;
import store.ApplicationFormStore;
import testsupport.StoreTestSupport;

/**
 * Unit tests for {@link ApplicationReviewServiceImpl} in the TA Recruitment
 * system. Verifies review picking, publishing, and application form filtering
 * behavior.
 */
class ApplicationReviewServiceImplTest {
    /** Temporary directory used for file-based store overrides. */
    @TempDir
    Path tempDir;

    /** The service implementation under test. */
    private final ApplicationReviewService service = new ApplicationReviewServiceImpl();

    /**
     * Sets up store files and system properties before each test, including
     * a course store with a default course entry and the application form
     * store path.
     */
    @BeforeEach
    void setUpStores() throws Exception {
        Path courseFile = StoreTestSupport.useCourseStore(tempDir);
        StoreTestSupport.useUserStore(tempDir);
        System.setProperty(ApplicationFormStore.FILE_PATH_PROPERTY, tempDir.resolve("application-forms.txt").toString());
        StoreTestSupport.writeLines(
                courseFile,
                "course-1,Software Engineering,TA,10 hours/week,TBD,Support labs,Communication skills");
    }

    /**
     * Clears store overrides and system properties after each test to prevent
     * state leakage between test cases.
     */
    @AfterEach
    void clearStoreOverrides() {
        StoreTestSupport.clearStoreOverrides();
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
    }

    /**
     * Tests that {@code saveReviewPicks} only processes application forms that
     * have been submitted (i.e., not draft forms), ensuring draft applicants
     * are excluded from the picked list and the submitted form count.
     */
    @Test
    void reviewOnlyUsesSubmittedApplicationForms() {
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA submittedApplicant = applicant("alice@example.com", course);
        TA draftApplicant = applicant("bob@example.com", course);
        course.addApplication(submittedApplicant, course.getId());
        course.addApplication(draftApplicant, course.getId());

        ApplicationForm submittedForm = form("alice@example.com", "course-1", true);
        ApplicationForm draftForm = form("bob@example.com", "course-1", false);
        ApplicationFormStore.saveOrUpdate(submittedForm);
        ApplicationFormStore.saveOrUpdate(draftForm);

        service.saveReviewPicks(course, new String[] { "alice@example.com", "bob@example.com" });

        assertEquals(1, service.getSubmittedFormsByApplicantEmail(course).size());
        assertTrue(course.getPickedApplicantEmails().contains("alice@example.com"));
        assertFalse(course.getPickedApplicantEmails().contains("bob@example.com"));
    }

    /**
     * Tests that {@code publishReview} updates the resume status to
     * {@code STATUS_APPROVED} for submitted forms and marks the review as
     * unread for those applicants, while draft forms retain
     * {@code STATUS_PENDING} and remain unread.
     */
    @Test
    void publishUpdatesStatusesForSubmittedFormsOnly() {
        Course course = new Course("course-1", "Software Engineering", "TA", "10 hours/week", "TBD", "Support labs", "Communication skills");
        TA submittedApplicant = applicant("alice@example.com", course);
        TA draftApplicant = applicant("bob@example.com", course);
        course.addApplication(submittedApplicant, course.getId());
        course.addApplication(draftApplicant, course.getId());

        ApplicationFormStore.saveOrUpdate(form("alice@example.com", "course-1", true));
        ApplicationFormStore.saveOrUpdate(form("bob@example.com", "course-1", false));

        service.publishReview(course, new String[] { "alice@example.com", "bob@example.com" });

        assertTrue(course.isReviewPublished());
        assertEquals(ResumeSubmission.STATUS_APPROVED, submittedApplicant.getResumeStatusForCourse("course-1"));
        assertTrue(submittedApplicant.isReviewUnreadForCourse("course-1"));
        assertEquals(ResumeSubmission.STATUS_PENDING, draftApplicant.getResumeStatusForCourse("course-1"));
        assertFalse(draftApplicant.isReviewUnreadForCourse("course-1"));
    }

    /**
     * Creates a TA applicant and links them to the given course.
     *
     * @param email  the applicant's email address
     * @param course the course to which the applicant applies
     * @return the created TA instance
     */
    private TA applicant(String email, Course course) {
        TA ta = new TA("secret", email);
        ta.addOrUpdateApplication(course, course.getId());
        return ta;
    }

    /**
     * Creates an {@link ApplicationForm} with sample pre-filled fields.
     *
     * @param email    the applicant's email address
     * @param courseId the ID of the target course
     * @param submitted whether the form is marked as submitted
     * @return the created ApplicationForm instance
     */
    private ApplicationForm form(String email, String courseId, boolean submitted) {
        ApplicationForm form = new ApplicationForm(email, courseId);
        form.setApplicantName(email);
        form.setEmail(email);
        form.setEducation("BSc Software Engineering");
        form.setSkills("Java");
        form.setRelevantExperience("Teaching lab support");
        form.setProjectExperience("Course project");
        form.setCourseFit("Strong fit");
        form.setFeedback("Private TA feedback");
        form.setSubmitted(submitted);
        return form;
    }
}
