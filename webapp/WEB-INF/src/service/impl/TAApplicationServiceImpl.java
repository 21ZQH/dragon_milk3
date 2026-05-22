package service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import service.ResumeStorageService;
import service.TAApplicationService;
import service.UserProfileService;

/**
 * Implementation of the {@link TAApplicationService} interface.
 * Provides concrete logic for managing TA applications, including
 * submitting resumes and application forms, validating application
 * limits, withdrawing applications, and preparing personal centre
 * data. Delegates to {@link UserProfileService} for profile updates
 * and {@link ResumeStorageService} for file operations.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class TAApplicationServiceImpl implements TAApplicationService {
    /** The maximum number of distinct TA applications a user can submit. */
    private static final int MAX_DISTINCT_APPLICATIONS = 3;

    /** Service for managing user profiles and course associations. */
    private final UserProfileService userProfileService;

    /** Service for resume file storage operations. */
    private final ResumeStorageService resumeStorageService;

    /**
     * Constructs a new {@code TAApplicationServiceImpl} with the given
     * services.
     *
     * @param userProfileService   the service for user profile operations
     * @param resumeStorageService the service for resume file storage
     */
    public TAApplicationServiceImpl(UserProfileService userProfileService, ResumeStorageService resumeStorageService) {
        this.userProfileService = userProfileService;
        this.resumeStorageService = resumeStorageService;
    }

    /**
     * Retrieves a course from the given list by its index parameter string.
     *
     * @param courseList        the list of courses to search
     * @param courseIndexParam  the string representation of the course index
     * @return the {@link Course} at the specified index, or {@code null}
     *         if the index is invalid or the parameter is malformed
     */
    @Override
    public Course getCourseByIndex(List<Course> courseList, String courseIndexParam) {
        if (courseIndexParam == null || courseList == null) {
            return null;
        }
        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                return null;
            }
            return courseList.get(courseIndex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Retrieves a course from the given list by its unique identifier.
     *
     * @param courseList the list of courses to search
     * @param courseId   the unique identifier of the course
     * @return the matching {@link Course}, or {@code null} if not found
     */
    @Override
    public Course getCourseById(List<Course> courseList, String courseId) {
        if (courseList == null || courseId == null || courseId.isBlank()) {
            return null;
        }
        for (Course course : courseList) {
            if (course != null && courseId.equals(course.getId())) {
                return course;
            }
        }
        return null;
    }

    /**
     * Finds the index of a course in the given list by its unique identifier.
     *
     * @param courseList the list of courses to search
     * @param courseId   the unique identifier of the course
     * @return the index of the matching course, or {@code null} if not found
     */
    @Override
    public Integer findCourseIndexById(List<Course> courseList, String courseId) {
        if (courseList == null || courseId == null || courseId.isBlank()) {
            return null;
        }
        for (int i = 0; i < courseList.size(); i++) {
            Course course = courseList.get(i);
            if (course != null && courseId.equals(course.getId())) {
                return i;
            }
        }
        return null;
    }

    /**
     * Checks whether the TA has a current resume file stored for the given course.
     *
     * @param ta     the TA applicant
     * @param course the course to check
     * @return {@code true} if a resume file exists for the course, {@code false} otherwise
     */
    @Override
    public boolean hasCurrentResume(TA ta, Course course) {
        if (course == null) {
            return false;
        }
        File resumeFile = resumeStorageService.getTaResumeFile(ta, course.getId());
        return resumeFile != null && resumeFile.exists() && resumeFile.isFile();
    }

    /**
     * Checks whether the TA has a master resume file stored.
     *
     * @param ta the TA to check
     * @return {@code true} if a master resume file exists, {@code false} otherwise
     */
    @Override
    public boolean hasMasterResume(TA ta) {
        File resumeFile = resumeStorageService.getMasterResumeFile(ta);
        return resumeFile != null && resumeFile.exists() && resumeFile.isFile();
    }

    /**
     * Retrieves the master resume file for the given TA.
     *
     * @param ta the TA whose master resume is to be retrieved
     * @return the master resume {@link File}, or {@code null} if not available
     */
    @Override
    public File getMasterResumeFile(TA ta) {
        return resumeStorageService.getMasterResumeFile(ta);
    }

    /**
     * Returns the stored resume file name for the given TA.
     *
     * @param ta the TA whose resume file name is requested
     * @return the normalized resume file name
     */
    @Override
    public String getStoredResumeFileName(TA ta) {
        return resumeStorageService.buildStoredResumeFileName(ta);
    }

    /**
     * Prepares the current application data for a TA applying to a course.
     *
     * @param ta     the TA applicant
     * @param course the course being applied to
     * @return a {@link CurrentApplicationData} object containing the
     *         application state
     */
    @Override
    public CurrentApplicationData prepareCurrentApplicationData(TA ta, Course course) {
        boolean hasCurrentApplication = false;
        if (ta != null && course != null) {
            String applicationFormId = ta.getApplicationFormIdForCourse(course.getId());
            hasCurrentApplication = applicationFormId != null && !applicationFormId.isBlank();
        }
        return new CurrentApplicationData(
                hasMasterResume(ta),
                getStoredResumeFileName(ta),
                hasCurrentApplication,
                hasCurrentApplication ? "Submitted application form" : null);
    }

    /**
     * Refreshes the TA object by retrieving the latest version from the
     * data store.
     *
     * @param ta the TA to refresh
     * @return the refreshed {@link TA} from the data store, or the original
     *         if not found
     */
    @Override
    public TA refreshTa(TA ta) {
        if (ta == null || ta.getEmail() == null || ta.getEmail().isBlank()) {
            return ta;
        }

        for (TA savedTa : userProfileService.getTAList()) {
            if (savedTa != null && ta.getEmail().equals(savedTa.getEmail())) {
                return savedTa;
            }
        }
        return ta;
    }

    /**
     * Returns the maximum number of distinct applications a TA can submit.
     *
     * @return the application limit (3)
     */
    @Override
    public int getApplicationLimit() {
        return MAX_DISTINCT_APPLICATIONS;
    }

    /**
     * Returns the number of courses the TA has already applied to.
     *
     * @param ta the TA whose application count is requested
     * @return the number of distinct applications
     */
    @Override
    public int getApplicationCount(TA ta) {
        return ta == null || ta.getAppliedClasses() == null ? 0 : ta.getAppliedClasses().size();
    }

    /**
     * Validates whether the TA is allowed to apply for the given course,
     * checking the maximum distinct application limit.
     *
     * @param ta     the TA applicant
     * @param course the course to validate against
     * @return a {@link SubmitApplicationResult} indicating success or the
     *         reason for failure
     */
    @Override
    public SubmitApplicationResult validateApplicationLimit(TA ta, Course course) {
        if (ta == null || course == null) {
            return SubmitApplicationResult.error("Unable to start this application.");
        }
        if (isNewCourseApplication(ta, course) && getApplicationCount(ta) >= MAX_DISTINCT_APPLICATIONS) {
            return SubmitApplicationResult.error("You can apply for up to 3 different TA positions.");
        }
        return SubmitApplicationResult.success();
    }

    /**
     * Submits a resume for a TA's application to a course. If a new resume
     * part is provided, it is uploaded as the master resume first.
     *
     * @param ta         the TA applicant
     * @param course     the course being applied to
     * @param resumePart the uploaded resume file part (may be {@code null})
     * @return a {@link SubmitResumeResult} indicating success or the reason for failure
     * @throws IOException if resume file storage fails
     */
    @Override
    public SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException {
        SubmitApplicationResult applicationLimitResult = validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            return SubmitResumeResult.error(applicationLimitResult.getErrorMessage());
        }

        String resumeDirectory = ta == null ? null : ta.getMasterResumeDirectory();
        String submittedFileName = "profile resume";

        if (resumePart != null && resumePart.getSize() > 0) {
            SubmitResumeResult uploadResult = uploadMasterResume(ta, resumePart);
            if (!uploadResult.isSuccess()) {
                return uploadResult;
            }
            resumeDirectory = ta.getMasterResumeDirectory();
            submittedFileName = uploadResult.getSubmittedFileName();
        }

        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            return SubmitResumeResult.error("Please upload your resume before submitting.");
        }

        String applicationFormId = course.getId();
        ta.addOrUpdateApplication(course, applicationFormId, ResumeSubmission.STATUS_PENDING);
        course.addApplication(ta, applicationFormId);
        userProfileService.updateAppliedCourseIds(ta);
        return SubmitResumeResult.success(submittedFileName);
    }

    /**
     * Submits an application form for a TA's application to a course.
     *
     * @param ta     the TA applicant
     * @param course the course being applied to
     * @param form   the completed application form
     * @return a {@link SubmitApplicationResult} indicating success or the
     *         reason for failure
     */
    @Override
    public SubmitApplicationResult submitApplicationForm(TA ta, Course course, ApplicationForm form) {
        if (ta == null || course == null) {
            return SubmitApplicationResult.error("Unable to submit this application.");
        }
        SubmitApplicationResult applicationLimitResult = validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            return applicationLimitResult;
        }
        String applicationFormId = form == null || form.getCourseId() == null || form.getCourseId().isBlank()
                ? course.getId()
                : form.getCourseId();
        ta.addOrUpdateApplication(course, applicationFormId, ResumeSubmission.STATUS_PENDING);
        course.addApplication(ta, applicationFormId);
        userProfileService.updateAppliedCourseIds(ta);
        return SubmitApplicationResult.success();
    }

    /**
     * Uploads a master resume for the TA, validating that the file is a PDF.
     *
     * @param ta         the TA whose master resume is being uploaded
     * @param resumePart the uploaded file part
     * @return a {@link SubmitResumeResult} indicating success or the reason for failure
     * @throws IOException if file storage fails
     */
    @Override
    public SubmitResumeResult uploadMasterResume(TA ta, Part resumePart) throws IOException {
        if (resumePart == null || resumePart.getSize() <= 0) {
            return SubmitResumeResult.error("Please upload your resume before submitting.");
        }
        String submittedFileName = resumePart.getSubmittedFileName();
        submittedFileName = submittedFileName == null ? "" : new File(submittedFileName).getName();
        if (!submittedFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return SubmitResumeResult.error("Only PDF resumes are accepted.");
        }

        String resumeDirectory = resumeStorageService.storeMasterResume(resumePart, ta);
        ta.setMasterResumeDirectory(resumeDirectory);
        userProfileService.updateTaProfile(ta);
        return SubmitResumeResult.success(submittedFileName);
    }

    /**
     * Updates the TA's profile with the given personal information and
     * optional skill selections.
     *
     * @param ta                 the TA whose profile is being updated
     * @param name               the display name
     * @param college            the college affiliation
     * @param skillFormSubmitted whether the skill form was submitted
     * @param selectedSkills     an array of selected skill strings
     */
    @Override
    public void updateProfile(TA ta, String name, String college, boolean skillFormSubmitted, String[] selectedSkills) {
        ta.setName(trimValue(name));
        ta.setCollege(trimValue(college));

        if (skillFormSubmitted) {
            List<String> skills = new ArrayList<>();
            if (selectedSkills != null) {
                for (String skill : selectedSkills) {
                    String trimmedSkill = trimValue(skill);
                    if (!trimmedSkill.isEmpty()) {
                        skills.add(trimmedSkill);
                    }
                }
            }
            ta.setSkill(String.join(", ", skills));
        }
        userProfileService.updateTaProfile(ta);
    }

    /**
     * Withdraws a TA's application for the given course.
     *
     * @param ta     the TA applicant
     * @param course the course from which to withdraw
     * @return a {@link WithdrawApplicationResult} indicating the outcome
     */
    @Override
    public WithdrawApplicationResult withdrawApplication(TA ta, Course course) {
        if (ta == null || course == null) {
            return new WithdrawApplicationResult(true);
        }
        ta.withdrawApplication(course.getId());
        course.removeApplicationByTaEmail(ta.getEmail());
        userProfileService.updateAppliedCourseIds(ta);
        return new WithdrawApplicationResult(true);
    }

    /**
     * Prepares data for the TA's personal centre view, including applied
     * courses, selected course details, resume status, and unread review
     * indicators.
     *
     * @param ta               the TA whose personal centre data is being prepared
     * @param selectedCourseId the currently selected course ID (may be {@code null})
     * @param applicationOpen  whether the application period is still open
     * @return a {@link PersonalCentreData} object with the aggregated data
     */
    @Override
    public PersonalCentreData preparePersonalCentreData(TA ta, String selectedCourseId, boolean applicationOpen) {
        List<Course> appliedCourses = ta.getAppliedClasses();
        Course selectedCourse = resolveSelectedAppliedCourse(appliedCourses, selectedCourseId);
        Integer selectedStatus = selectedCourse == null ? null : resolveResumeStatus(ta, selectedCourse.getId());
        Set<String> unreadReviewCourseIds = resolveUnreadReviewCourseIds(ta, appliedCourses);
        if (ta.markAllReviewUpdatesRead()) {
            userProfileService.updateAppliedCourseIds(ta);
        }
        return new PersonalCentreData(
                appliedCourses,
                selectedCourse,
                selectedStatus,
                unreadReviewCourseIds,
                applicationOpen,
                getApplicationCount(ta),
                MAX_DISTINCT_APPLICATIONS);
    }

    /**
     * Resolves the set of course IDs for which the TA has unread review updates.
     *
     * @param ta             the TA applicant
     * @param appliedCourses the list of courses the TA has applied to
     * @return a {@link Set} of course IDs with unread reviews
     */
    private Set<String> resolveUnreadReviewCourseIds(TA ta, List<Course> appliedCourses) {
        Set<String> unreadCourseIds = new LinkedHashSet<>();
        if (ta == null || appliedCourses == null) {
            return unreadCourseIds;
        }

        for (Course course : appliedCourses) {
            if (course != null && course.getId() != null && ta.isReviewUnreadForCourse(course.getId())) {
                unreadCourseIds.add(course.getId());
            }
        }
        return unreadCourseIds;
    }

    /**
     * Resolves the selected course from the applied courses list. If no
     * specific selection is provided, returns the first applied course.
     *
     * @param appliedCourses   the list of courses the TA has applied to
     * @param selectedCourseId the ID of the course to select (may be {@code null})
     * @return the selected {@link Course}, or the first course if none selected,
     *         or {@code null} if the list is empty
     */
    private Course resolveSelectedAppliedCourse(List<Course> appliedCourses, String selectedCourseId) {
        if (appliedCourses == null || appliedCourses.isEmpty()) {
            return null;
        }
        if (selectedCourseId != null && !selectedCourseId.isBlank()) {
            for (Course course : appliedCourses) {
                if (course != null && selectedCourseId.equals(course.getId())) {
                    return course;
                }
            }
        }
        return appliedCourses.get(0);
    }

    /**
     * Checks whether the TA has not yet applied to the given course.
     *
     * @param ta     the TA applicant
     * @param course the course to check
     * @return {@code true} if the application is new, {@code false} otherwise
     */
    private boolean isNewCourseApplication(TA ta, Course course) {
        return ta.getApplicationFormIdForCourse(course.getId()) == null;
    }

    /**
     * Resolves the resume status for a TA's application to a course, defaulting
     * to {@link ResumeSubmission#STATUS_PENDING} if no status is set.
     *
     * @param ta       the TA applicant
     * @param courseId the course ID to check
     * @return the resume status code
     */
    private int resolveResumeStatus(TA ta, String courseId) {
        Integer status = ta.getResumeStatusForCourse(courseId);
        return status == null ? ResumeSubmission.STATUS_PENDING : status;
    }

    /**
     * Trims a string value, returning an empty string for null input.
     *
     * @param value the string to trim
     * @return the trimmed string, or an empty string if the input is {@code null}
     */
    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }
}
