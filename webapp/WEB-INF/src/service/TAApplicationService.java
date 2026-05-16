package service;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.Part;
import model.Course;
import model.TA;

public interface TAApplicationService {
    Course getCourseByIndex(List<Course> courseList, String courseIndexParam);

    Course getCourseById(List<Course> courseList, String courseId);

    Integer findCourseIndexById(List<Course> courseList, String courseId);

    boolean hasCurrentResume(TA ta, Course course);

    SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException;

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

    final class PersonalCentreData {
        private final List<Course> appliedCourses;
        private final Course selectedCourse;
        private final Integer selectedStatus;
        private final boolean applicationOpen;

        public PersonalCentreData(List<Course> appliedCourses, Course selectedCourse,
                Integer selectedStatus, boolean applicationOpen) {
            this.appliedCourses = appliedCourses;
            this.selectedCourse = selectedCourse;
            this.selectedStatus = selectedStatus;
            this.applicationOpen = applicationOpen;
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
    }
}
