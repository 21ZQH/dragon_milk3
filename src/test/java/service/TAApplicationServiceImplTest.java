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

class TAApplicationServiceImplTest {
    @TempDir
    Path tempDir;

    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final ResumeStorageService resumeStorageService = mock(ResumeStorageService.class);
    private final TAApplicationService service =
            new TAApplicationServiceImpl(userProfileService, resumeStorageService);

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

    @Test
    void withdrawApplicationIgnoresMissingCourseButKeepsResultSuccessful() {
        TA ta = new TA("secret123", "ta@example.com");

        TAApplicationService.WithdrawApplicationResult result = service.withdrawApplication(ta, null);

        assertTrue(result.isResumeDeleted());
        assertEquals(0, ta.getAppliedClasses().size());
    }

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

    private Course course(String id) {
        return new Course(id, "Course " + id, "TA", "10 hours/week", "TBD", "Support", "Requirement");
    }
}
