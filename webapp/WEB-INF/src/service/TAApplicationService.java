package service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.TA;

/**
 * Service interface for managing TA applications, including course selection,
 * resume uploads, application form submission, profile updates, and withdrawal.
 * Provides comprehensive support for the full TA application lifecycle with
 * supporting data classes for results and presentation.
 *
 * @version 1.0
 * @since 2025
 */
public interface TAApplicationService {
    /**
     * Retrieves a course from the given list by its index string parameter.
     *
     * @param courseList        the list of courses to search
     * @param courseIndexParam  the course index as a string parameter
     * @return the matching {@link Course}, or {@code null} if not found
     */
    Course getCourseByIndex(List<Course> courseList, String courseIndexParam);

    /**
     * Retrieves a course from the given list by its unique identifier.
     *
     * @param courseList the list of courses to search
     * @param courseId   the unique course identifier
     * @return the matching {@link Course}, or {@code null} if not found
     */
    Course getCourseById(List<Course> courseList, String courseId);

    /**
     * Finds the index of a course in the given list by its identifier.
     *
     * @param courseList the list of courses to search
     * @param courseId   the unique course identifier
     * @return the index of the course, or {@code null} if not found
     */
    Integer findCourseIndexById(List<Course> courseList, String courseId);

    /**
     * Checks whether the TA currently has a resume submitted for a specific course.
     *
     * @param ta     the TA to check
     * @param course the course to check
     * @return {@code true} if the TA has a current resume for the course,
     *         {@code false} otherwise
     */
    boolean hasCurrentResume(TA ta, Course course);

    /**
     * Checks whether the TA has a master resume on file.
     *
     * @param ta the TA to check
     * @return {@code true} if a master resume exists, {@code false} otherwise
     */
    boolean hasMasterResume(TA ta);

    /**
     * Retrieves the master resume file for the specified TA.
     *
     * @param ta the TA whose master resume to retrieve
     * @return the {@link File} object for the master resume
     */
    File getMasterResumeFile(TA ta);

    /**
     * Returns the stored resume file name for the specified TA.
     *
     * @param ta the TA whose resume file name to retrieve
     * @return the stored resume file name as a string
     */
    String getStoredResumeFileName(TA ta);

    /**
     * Prepares the current application data for a TA applying to a specific course,
     * including resume status and application details.
     *
     * @param ta     the TA to prepare data for
     * @param course the course being applied to
     * @return a {@link CurrentApplicationData} object with the relevant information
     */
    CurrentApplicationData prepareCurrentApplicationData(TA ta, Course course);

    /**
     * Refreshes the TA data from the persistent store to reflect the latest state.
     *
     * @param ta the TA to refresh
     * @return a refreshed {@link TA} object
     */
    TA refreshTa(TA ta);

    /**
     * Returns the maximum number of applications a TA is allowed to submit.
     *
     * @return the application limit as an integer
     */
    int getApplicationLimit();

    /**
     * Returns the current number of applications submitted by the specified TA.
     *
     * @param ta the TA whose application count to retrieve
     * @return the current application count
     */
    int getApplicationCount(TA ta);

    /**
     * Validates whether the TA is allowed to apply to the given course
     * based on the application limit.
     *
     * @param ta     the TA attempting to apply
     * @param course the course being applied to
     * @return a {@link SubmitApplicationResult} indicating whether the
     *         application limit allows the submission
     */
    SubmitApplicationResult validateApplicationLimit(TA ta, Course course);

    /**
     * Submits a resume for a TA applying to a specific course.
     *
     * @param ta         the TA submitting the resume
     * @param course     the course being applied to
     * @param resumePart the uploaded resume file part from the HTTP request
     * @return a {@link SubmitResumeResult} indicating success or failure
     * @throws IOException if an I/O error occurs during resume storage
     */
    SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException;

    /**
     * Uploads a master resume for the specified TA.
     *
     * @param ta         the TA uploading the master resume
     * @param resumePart the uploaded resume file part from the HTTP request
     * @return a {@link SubmitResumeResult} indicating success or failure
     * @throws IOException if an I/O error occurs during resume storage
     */
    SubmitResumeResult uploadMasterResume(TA ta, Part resumePart) throws IOException;

    /**
     * Submits a completed application form for a TA applying to a course.
     *
     * @param ta     the TA submitting the form
     * @param course the course being applied to
     * @param form   the completed application form
     * @return a {@link SubmitApplicationResult} indicating success or failure
     */
    SubmitApplicationResult submitApplicationForm(TA ta, Course course, ApplicationForm form);

    /**
     * Updates the profile information for a TA, including name, college,
     * skill form status, and selected skills.
     *
     * @param ta                  the TA whose profile to update
     * @param name                the updated name
     * @param college             the updated college name
     * @param skillFormSubmitted  whether the skill form has been submitted
     * @param selectedSkills      an array of selected skill identifiers
     */
    void updateProfile(TA ta, String name, String college, boolean skillFormSubmitted, String[] selectedSkills);

    /**
     * Withdraws a TA's application for a specific course.
     *
     * @param ta     the TA withdrawing the application
     * @param course the course from which to withdraw
     * @return a {@link WithdrawApplicationResult} indicating the outcome
     */
    WithdrawApplicationResult withdrawApplication(TA ta, Course course);

    /**
     * Prepares the data needed for the TA's personal centre view,
     * including applied courses, selected course details, and unread review information.
     *
     * @param ta                  the TA whose personal centre data to prepare
     * @param selectedCourseId    the currently selected course identifier
     * @param applicationOpen     whether the application period is currently open
     * @return a {@link PersonalCentreData} object with the relevant information
     */
    PersonalCentreData preparePersonalCentreData(TA ta, String selectedCourseId, boolean applicationOpen);

    /**
     * Represents the result of a resume submission operation,
     * encapsulating success status, the submitted file name, and an error message on failure.
     */
    final class SubmitResumeResult {
        private final boolean success;
        private final String submittedFileName;
        private final String errorMessage;

        /**
         * Constructs a SubmitResumeResult with the given parameters.
         *
         * @param success            whether the submission was successful
         * @param submittedFileName  the name of the submitted file
         * @param errorMessage       an error message, or {@code null} on success
         */
        private SubmitResumeResult(boolean success, String submittedFileName, String errorMessage) {
            this.success = success;
            this.submittedFileName = submittedFileName;
            this.errorMessage = errorMessage;
        }

        /**
         * Creates a successful resume submission result.
         *
         * @param submittedFileName the name of the successfully submitted file
         * @return a success result with the given file name
         */
        public static SubmitResumeResult success(String submittedFileName) {
            return new SubmitResumeResult(true, submittedFileName, null);
        }

        /**
         * Creates a failed resume submission result with an error message.
         *
         * @param errorMessage the description of the failure
         * @return a failure result with the given error message
         */
        public static SubmitResumeResult error(String errorMessage) {
            return new SubmitResumeResult(false, null, errorMessage);
        }

        /**
         * Returns whether the resume submission was successful.
         *
         * @return {@code true} if successful, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the name of the submitted resume file.
         *
         * @return the submitted file name, or {@code null} on failure
         */
        public String getSubmittedFileName() {
            return submittedFileName;
        }

        /**
         * Returns the error message if the submission failed.
         *
         * @return the error message, or {@code null} on success
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Represents the result of an application withdrawal operation,
     * indicating whether the associated resume file was deleted.
     */
    final class WithdrawApplicationResult {
        private final boolean resumeDeleted;

        /**
         * Constructs a WithdrawApplicationResult with the given flag.
         *
         * @param resumeDeleted whether the resume file was successfully deleted
         */
        public WithdrawApplicationResult(boolean resumeDeleted) {
            this.resumeDeleted = resumeDeleted;
        }

        /**
         * Returns whether the resume file was deleted during withdrawal.
         *
         * @return {@code true} if the resume was deleted, {@code false} otherwise
         */
        public boolean isResumeDeleted() {
            return resumeDeleted;
        }
    }

    /**
     * Represents the result of an application submission operation,
     * indicating success or providing an error message on failure.
     */
    final class SubmitApplicationResult {
        private final boolean success;
        private final String errorMessage;

        /**
         * Constructs a SubmitApplicationResult with the given parameters.
         *
         * @param success      whether the submission was successful
         * @param errorMessage an error message, or {@code null} on success
         */
        private SubmitApplicationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        /**
         * Creates a successful application submission result.
         *
         * @return a success result
         */
        public static SubmitApplicationResult success() {
            return new SubmitApplicationResult(true, null);
        }

        /**
         * Creates a failed application submission result with an error message.
         *
         * @param errorMessage the description of the failure
         * @return a failure result with the given error message
         */
        public static SubmitApplicationResult error(String errorMessage) {
            return new SubmitApplicationResult(false, errorMessage);
        }

        /**
         * Returns whether the application submission was successful.
         *
         * @return {@code true} if successful, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the error message if the submission failed.
         *
         * @return the error message, or {@code null} on success
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Holds the current application state for a TA applying to a course,
     * including master resume status and current application details.
     */
    final class CurrentApplicationData {
        private final boolean hasMasterResume;
        private final String masterResumeFileName;
        private final boolean hasCurrentApplication;
        private final String currentApplicationFileName;

        /**
         * Constructs a CurrentApplicationData instance with the specified values.
         *
         * @param hasMasterResume            whether the TA has a master resume
         * @param masterResumeFileName       the name of the master resume file
         * @param hasCurrentApplication      whether the TA has a current application
         * @param currentApplicationFileName the name of the current application file
         */
        public CurrentApplicationData(boolean hasMasterResume, String masterResumeFileName,
                boolean hasCurrentApplication, String currentApplicationFileName) {
            this.hasMasterResume = hasMasterResume;
            this.masterResumeFileName = masterResumeFileName;
            this.hasCurrentApplication = hasCurrentApplication;
            this.currentApplicationFileName = currentApplicationFileName;
        }

        /**
         * Returns whether the TA has a master resume.
         *
         * @return {@code true} if a master resume exists
         */
        public boolean hasMasterResume() {
            return hasMasterResume;
        }

        /**
         * Returns the file name of the master resume.
         *
         * @return the master resume file name
         */
        public String getMasterResumeFileName() {
            return masterResumeFileName;
        }

        /**
         * Returns whether the TA has a current application for the course.
         *
         * @return {@code true} if a current application exists
         */
        public boolean hasCurrentApplication() {
            return hasCurrentApplication;
        }

        /**
         * Returns the file name of the current application.
         *
         * @return the current application file name
         */
        public String getCurrentApplicationFileName() {
            return currentApplicationFileName;
        }
    }

    /**
     * Holds the data required for rendering the TA's personal centre page,
     * including applied courses, selected course status, unread review flags,
     * and application limit information.
     */
    final class PersonalCentreData {
        private final List<Course> appliedCourses;
        private final Course selectedCourse;
        private final Integer selectedStatus;
        private final Set<String> unreadReviewCourseIds;
        private final boolean applicationOpen;
        private final int applicationCount;
        private final int applicationLimit;

        /**
         * Constructs a PersonalCentreData instance with the specified values.
         *
         * @param appliedCourses          the list of courses the TA has applied to
         * @param selectedCourse          the currently selected course for detailed view
         * @param selectedStatus          the status of the selected course application
         * @param unreadReviewCourseIds   the set of course IDs with unread review results
         * @param applicationOpen         whether the application period is open
         * @param applicationCount        the number of applications submitted
         * @param applicationLimit        the maximum allowed applications
         */
        public PersonalCentreData(List<Course> appliedCourses, Course selectedCourse,
                Integer selectedStatus, Set<String> unreadReviewCourseIds, boolean applicationOpen,
                int applicationCount, int applicationLimit) {
            this.appliedCourses = appliedCourses;
            this.selectedCourse = selectedCourse;
            this.selectedStatus = selectedStatus;
            this.unreadReviewCourseIds = unreadReviewCourseIds;
            this.applicationOpen = applicationOpen;
            this.applicationCount = applicationCount;
            this.applicationLimit = applicationLimit;
        }

        /**
         * Returns the list of courses the TA has applied to.
         *
         * @return a list of applied {@link Course} objects
         */
        public List<Course> getAppliedCourses() {
            return appliedCourses;
        }

        /**
         * Returns the currently selected course for detailed viewing.
         *
         * @return the selected {@link Course}, or {@code null} if none selected
         */
        public Course getSelectedCourse() {
            return selectedCourse;
        }

        /**
         * Returns the status of the selected course application.
         *
         * @return the application status as an Integer, or {@code null} if not applicable
         */
        public Integer getSelectedStatus() {
            return selectedStatus;
        }

        /**
         * Returns the set of course IDs for which the TA has unread review results.
         *
         * @return a set of course ID strings
         */
        public Set<String> getUnreadReviewCourseIds() {
            return unreadReviewCourseIds;
        }

        /**
         * Returns whether the application period is currently open.
         *
         * @return {@code true} if applications are being accepted
         */
        public boolean isApplicationOpen() {
            return applicationOpen;
        }

        /**
         * Returns the current number of submitted applications.
         *
         * @return the application count
         */
        public int getApplicationCount() {
            return applicationCount;
        }

        /**
         * Returns the maximum number of applications a TA is allowed to submit.
         *
         * @return the application limit
         */
        public int getApplicationLimit() {
            return applicationLimit;
        }
    }
}
