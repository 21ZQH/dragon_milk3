package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.impl.TAApplicationServiceImpl;

/**
 * Unit tests for {@link TAApplicationServiceImpl} in the TA Recruitment system.
 * Verifies master resume handling, application form submission, application
 * limits, withdrawal, and resume upload validation.
 */
class TAApplicationServiceImplTest {
    /** Temporary directory used for resume file operations. */
    @TempDir
    Path tempDir;

    /** Mocked user profile service dependency. */
    private final UserProfileService userProfileService = mock(UserProfileService.class);

    /** Mocked resume storage service dependency. */
    private final ResumeStorageService resumeStorageService = mock(ResumeStorageService.class);

    /** The service implementation under test with mocked dependencies injected. */
    private final TAApplicationService service =
            new TAApplicationServiceImpl(userProfileService, resumeStorageService);

    /**
     * Tests that a master resume is reported as present only when the
     * corresponding stored file actually exists on disk.
     */
    @Test
    void reportsMasterResumeOnlyWhenStoredFileExists() throws Exception {
        TA ta = new TA("secret123", "ta@example.com");
        Path resumeDirectory = tempDir.resolve("master");
        Files.createDirectories(resumeDirectory);
        File resumeFile = resumeDirectory.resolve("ta_example.com.pdf").toFile();
        Files.writeString(resumeFile.toPath(), "pdf");

        when(resumeStorageService.getMasterResumeFile(ta)).thenReturn(resumeFile);

        assertTrue(service.hasMasterResume(ta));
        assertEquals(resumeFile, service.getMasterResumeFile(ta));
    }

    /**
     * Tests that the current application data includes the submitted form
     * marker when the TA has an existing application for the given course.
     */
    @Test
    void currentApplicationDataUsesSubmittedFormMarker() {
        Course course = new Course("course-1", "Software Engineering", "TA",
                "10 hours/week", "TBD", "Support labs", "Java");
        TA ta = new TA("secret123", "ta@example.com");
        ta.addOrUpdateApplication(course, course.getId());
        when(resumeStorageService.buildStoredResumeFileName(ta)).thenReturn("ta_example.com.pdf");

        TAApplicationService.CurrentApplicationData data =
                service.prepareCurrentApplicationData(ta, course);

        assertFalse(data.hasMasterResume());
        assertEquals("ta_example.com.pdf", data.getMasterResumeFileName());
        assertTrue(data.hasCurrentApplication());
        assertEquals("Submitted application form", data.getCurrentApplicationFileName());
    }

    /**
     * Tests that submitting an application form links the TA to the course,
     * persists the TA's applied course IDs, and sets the resume status to
     * {@code STATUS_PENDING}.
     */
    @Test
    void submitApplicationFormLinksTaAndCourseAndPersistsAppliedCourseIds() {
        Course course = new Course("course-1", "Software Engineering", "TA",
                "10 hours/week", "TBD", "Support labs", "Java");
        TA ta = new TA("secret123", "ta@example.com");
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());

        TAApplicationService.SubmitApplicationResult result = service.submitApplicationForm(ta, course, form);

        assertTrue(result.isSuccess());
        assertEquals(course.getId(), ta.getApplicationFormIdForCourse(course.getId()));
        assertEquals(ResumeSubmission.STATUS_PENDING, ta.getResumeStatusForCourse(course.getId()));
        assertEquals(List.of(ta), course.getTaApplicants());
        assertEquals(List.of(course.getId()), course.getApplicantFormIds());
        verify(userProfileService).updateAppliedCourseIds(ta);
    }

    /**
     * Tests that submitting an application form for a fourth different course
     * is rejected, as TAs may apply for at most three different TA positions.
     */
    @Test
    void submitApplicationFormRejectsFourthDifferentCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course firstCourse = course("course-1");
        Course secondCourse = course("course-2");
        Course thirdCourse = course("course-3");
        Course fourthCourse = course("course-4");
        ta.addOrUpdateApplication(firstCourse, firstCourse.getId());
        ta.addOrUpdateApplication(secondCourse, secondCourse.getId());
        ta.addOrUpdateApplication(thirdCourse, thirdCourse.getId());
        ApplicationForm form = new ApplicationForm(ta.getEmail(), fourthCourse.getId());

        TAApplicationService.SubmitApplicationResult result =
                service.submitApplicationForm(ta, fourthCourse, form);

        assertFalse(result.isSuccess());
        assertEquals("You can apply for up to 3 different TA positions.", result.getErrorMessage());
        assertNull(ta.getApplicationFormIdForCourse(fourthCourse.getId()));
        assertEquals(0, fourthCourse.getTaApplicants().size());
        verify(userProfileService, never()).updateAppliedCourseIds(ta);
    }

    /**
     * Tests that the application limit validation rejects a TA from starting
     * an application for a fourth different course and returns the correct
     * current application count and limit.
     */
    @Test
    void validateApplicationLimitRejectsStartingFourthDifferentCourse() {
        TA ta = new TA("secret123", "ta@example.com");
        Course firstCourse = course("course-1");
        Course secondCourse = course("course-2");
        Course thirdCourse = course("course-3");
        Course fourthCourse = course("course-4");
        ta.addOrUpdateApplication(firstCourse, firstCourse.getId());
        ta.addOrUpdateApplication(secondCourse, secondCourse.getId());
        ta.addOrUpdateApplication(thirdCourse, thirdCourse.getId());

        TAApplicationService.SubmitApplicationResult result =
                service.validateApplicationLimit(ta, fourthCourse);

        assertFalse(result.isSuccess());
        assertEquals("You can apply for up to 3 different TA positions.", result.getErrorMessage());
        assertEquals(3, service.getApplicationCount(ta));
        assertEquals(3, service.getApplicationLimit());
    }

    /**
     * Tests that the application limit validation allows a TA to apply for a
     * course they have already applied to, even when at the three-course limit.
     */
    @Test
    void validateApplicationLimitAllowsExistingCourseAtLimit() {
        TA ta = new TA("secret123", "ta@example.com");
        Course firstCourse = course("course-1");
        Course secondCourse = course("course-2");
        Course thirdCourse = course("course-3");
        ta.addOrUpdateApplication(firstCourse, firstCourse.getId());
        ta.addOrUpdateApplication(secondCourse, secondCourse.getId());
        ta.addOrUpdateApplication(thirdCourse, thirdCourse.getId());

        TAApplicationService.SubmitApplicationResult result =
                service.validateApplicationLimit(ta, thirdCourse);

        assertTrue(result.isSuccess());
    }

    /**
     * Tests that the personal centre data includes the correct application
     * count and the configured application limit.
     */
    @Test
    void personalCentreDataIncludesApplicationCountAndLimit() {
        TA ta = new TA("secret123", "ta@example.com");
        Course firstCourse = course("course-1");
        Course secondCourse = course("course-2");
        ta.addOrUpdateApplication(firstCourse, firstCourse.getId());
        ta.addOrUpdateApplication(secondCourse, secondCourse.getId());

        TAApplicationService.PersonalCentreData data =
                service.preparePersonalCentreData(ta, null, true);

        assertEquals(2, data.getApplicationCount());
        assertEquals(3, data.getApplicationLimit());
    }

    /**
     * Tests that a TA can resubmit an application form for a course they have
     * already applied to, even when currently at the three-course limit.
     */
    @Test
    void submitApplicationFormAllowsResubmittingExistingCourseWhenAtLimit() {
        TA ta = new TA("secret123", "ta@example.com");
        Course firstCourse = course("course-1");
        Course secondCourse = course("course-2");
        Course thirdCourse = course("course-3");
        ta.addOrUpdateApplication(firstCourse, firstCourse.getId());
        ta.addOrUpdateApplication(secondCourse, secondCourse.getId());
        ta.addOrUpdateApplication(thirdCourse, thirdCourse.getId());
        ApplicationForm form = new ApplicationForm(ta.getEmail(), thirdCourse.getId());

        TAApplicationService.SubmitApplicationResult result =
                service.submitApplicationForm(ta, thirdCourse, form);

        assertTrue(result.isSuccess());
        assertEquals(3, ta.getAppliedClasses().size());
        assertEquals(thirdCourse.getId(), ta.getApplicationFormIdForCourse(thirdCourse.getId()));
        verify(userProfileService).updateAppliedCourseIds(ta);
    }

    /**
     * Tests that withdrawing an application for a null (missing) course still
     * returns a successful result and leaves the TA with no applied classes.
     */
    @Test
    void withdrawApplicationIgnoresMissingCourseButKeepsResultSuccessful() {
        TA ta = new TA("secret123", "ta@example.com");

        TAApplicationService.WithdrawApplicationResult result = service.withdrawApplication(ta, null);

        assertTrue(result.isResumeDeleted());
        assertEquals(0, ta.getAppliedClasses().size());
    }

    /**
     * Tests that uploading a non-PDF file as a master resume is rejected
     * with an appropriate error message and does not update the TA's resume
     * directory.
     */
    @Test
    void rejectsNonPdfMasterResumeInService() throws Exception {
        TA ta = new TA("secret123", "ta@example.com");
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(100L);
        when(part.getSubmittedFileName()).thenReturn("resume.docx");

        TAApplicationService.SubmitResumeResult result = service.uploadMasterResume(ta, part);

        assertFalse(result.isSuccess());
        assertEquals("Only PDF resumes are accepted.", result.getErrorMessage());
        assertNull(ta.getMasterResumeDirectory());
    }

    /**
     * Creates a {@link Course} with the given ID and default properties.
     *
     * @param id the course ID
     * @return the created Course instance
     */
    private Course course(String id) {
        return new Course(id, "Course " + id, "TA", "10 hours/week", "TBD", "Support", "Requirement");
    }
}
