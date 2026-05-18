package service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.TA;

public interface TAApplicationService {
    Course getCourseByIndex(List<Course> courseList, String courseIndexParam);

    Course getCourseById(List<Course> courseList, String courseId);

    Integer findCourseIndexById(List<Course> courseList, String courseId);

    boolean hasCurrentResume(TA ta, Course course);

    boolean hasMasterResume(TA ta);

    File getMasterResumeFile(TA ta);

    String getStoredResumeFileName(TA ta);

    CurrentApplicationData prepareCurrentApplicationData(TA ta, Course course);

    int getApplicationLimit();

    int getApplicationCount(TA ta);

    SubmitApplicationResult validateApplicationLimit(TA ta, Course course);

    SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException;

    SubmitResumeResult uploadMasterResume(TA ta, Part resumePart) throws IOException;

    SubmitApplicationResult submitApplicationForm(TA ta, Course course, ApplicationForm form);

    void updateProfile(TA ta, String name, String college, boolean skillFormSubmitted, String[] selectedSkills);

    WithdrawApplicationResult withdrawApplication(TA ta, Course course);

    PersonalCentreData preparePersonalCentreData(TA ta, String selectedCourseId, boolean applicationOpen);

    final class SubmitResumeResult {
        private final boolean success;
        private final String submittedFileName;
        private final String errorMessage;

        private SubmitResumeResult(boolean success, String submittedFileName, String errorMessage) {
            this.success = success;
            this.submittedFileName = submittedFileName;
            this.errorMessage = errorMessage;
        }

        public static SubmitResumeResult success(String submittedFileName) {
            return new SubmitResumeResult(true, submittedFileName, null);
        }

        public static SubmitResumeResult error(String errorMessage) {
            return new SubmitResumeResult(false, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSubmittedFileName() {
            return submittedFileName;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    final class WithdrawApplicationResult {
        private final boolean resumeDeleted;

        public WithdrawApplicationResult(boolean resumeDeleted) {
            this.resumeDeleted = resumeDeleted;
        }

        public boolean isResumeDeleted() {
            return resumeDeleted;
        }
    }

    final class SubmitApplicationResult {
        private final boolean success;
        private final String errorMessage;

        private SubmitApplicationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static SubmitApplicationResult success() {
            return new SubmitApplicationResult(true, null);
        }

        public static SubmitApplicationResult error(String errorMessage) {
            return new SubmitApplicationResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    final class CurrentApplicationData {
        private final boolean hasMasterResume;
        private final String masterResumeFileName;
        private final boolean hasCurrentApplication;
        private final String currentApplicationFileName;

        public CurrentApplicationData(boolean hasMasterResume, String masterResumeFileName,
                boolean hasCurrentApplication, String currentApplicationFileName) {
            this.hasMasterResume = hasMasterResume;
            this.masterResumeFileName = masterResumeFileName;
            this.hasCurrentApplication = hasCurrentApplication;
            this.currentApplicationFileName = currentApplicationFileName;
        }

        public boolean hasMasterResume() {
            return hasMasterResume;
        }

        public String getMasterResumeFileName() {
            return masterResumeFileName;
        }

        public boolean hasCurrentApplication() {
            return hasCurrentApplication;
        }

        public String getCurrentApplicationFileName() {
            return currentApplicationFileName;
        }
    }

    final class PersonalCentreData {
        private final List<Course> appliedCourses;
        private final Course selectedCourse;
        private final Integer selectedStatus;
        private final boolean applicationOpen;
        private final int applicationCount;
        private final int applicationLimit;

        public PersonalCentreData(List<Course> appliedCourses, Course selectedCourse,
                Integer selectedStatus, boolean applicationOpen, int applicationCount, int applicationLimit) {
            this.appliedCourses = appliedCourses;
            this.selectedCourse = selectedCourse;
            this.selectedStatus = selectedStatus;
            this.applicationOpen = applicationOpen;
            this.applicationCount = applicationCount;
            this.applicationLimit = applicationLimit;
        }

        public List<Course> getAppliedCourses() {
            return appliedCourses;
        }

        public Course getSelectedCourse() {
            return selectedCourse;
        }

        public Integer getSelectedStatus() {
            return selectedStatus;
        }

        public boolean isApplicationOpen() {
            return applicationOpen;
        }

        public int getApplicationCount() {
            return applicationCount;
        }

        public int getApplicationLimit() {
            return applicationLimit;
        }
    }
}
