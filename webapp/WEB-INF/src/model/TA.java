package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Teaching assistant account and application state.
 *
 * <p>A TA owns profile details, a master resume path, applied courses, and a
 * list of {@link ResumeSubmission} records. The submission records track the
 * per-course application form id, review result, and unread review notification
 * flag shown as red dots in the TA interface.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class TA extends User {
    /** The fixed role identifier for teaching assistant accounts. */
    private String role = "TA";
    /** The college or department the TA belongs to. */
    private String college;
    /** The skills or areas of expertise of the TA. */
    private String skill;
    /** The file system directory path to the TA's master resume. */
    private String masterResumeDirectory;
    /** The list of courses to which the TA has applied. */
    private List<Course> appliedClasses = new ArrayList<>();
    /** The list of resume submissions tracking per-course application status. */
    private List<ResumeSubmission> resumeSubmissions = new ArrayList<>();

    /**
     * Constructs a TA account with the specified credentials.
     *
     * @param password the login password for the TA account
     * @param email    the email address to associate with this TA account
     */
    public TA(String password, String email) {
        super(password, email);
    }

    /**
     * Returns the role identifier for this teaching assistant.
     *
     * @return the string "TA" indicating this is a teaching assistant account
     */
    @Override
    public String getRole() {
        return role;
    }

    /**
     * Returns the college or department of the TA.
     *
     * @return the college name, or {@code null} if not set
     */
    public String getCollege() {
        return college;
    }

    /**
     * Sets the college or department of the TA.
     *
     * @param college the college name to set
     */
    public void setCollege(String college) {
        this.college = college;
    }

    /**
     * Returns the skills or areas of expertise of the TA.
     *
     * @return the skill description, or {@code null} if not set
     */
    public String getSkill() {
        return skill;
    }

    /**
     * Sets the skills or areas of expertise of the TA.
     *
     * @param skill the skill description to set
     */
    public void setSkill(String skill) {
        this.skill = skill;
    }

    /**
     * Returns the file system directory path to the TA's master resume.
     *
     * @return the master resume directory path, or {@code null} if not set
     */
    public String getMasterResumeDirectory() {
        return masterResumeDirectory;
    }

    /**
     * Sets the file system directory path to the TA's master resume.
     *
     * @param masterResumeDirectory the master resume directory path to set
     */
    public void setMasterResumeDirectory(String masterResumeDirectory) {
        this.masterResumeDirectory = masterResumeDirectory;
    }

    /**
     * Returns the list of courses to which the TA has applied.
     *
     * @return the list of applied courses
     */
    public List<Course> getAppliedClasses() {
        return appliedClasses;
    }

    /**
     * Sets the list of courses to which the TA has applied.
     * <p>This method clears existing applied classes and populates using
     * {@link #addClass(Course)} to prevent duplicate entries.</p>
     *
     * @param appliedClasses the list of courses to set as applied
     */
    public void setAppliedClasses(List<Course> appliedClasses) {
        this.appliedClasses = new ArrayList<>();
        if (appliedClasses != null) {
            for (Course course : appliedClasses) {
                addClass(course);
            }
        }
    }

    /**
     * Returns the list of resume submissions for this TA.
     *
     * @return the list of resume submissions
     */
    public List<ResumeSubmission> getResumeSubmissions() {
        return resumeSubmissions;
    }

    /**
     * Sets the list of resume submissions for this TA.
     * <p>This method clears existing submissions and repopulates using
     * {@link #addOrUpdateApplication(Course, String, int, boolean)} to ensure
     * consistency.</p>
     *
     * @param resumeSubmissions the list of resume submissions to set
     */
    public void setResumeSubmissions(List<ResumeSubmission> resumeSubmissions) {
        this.resumeSubmissions = new ArrayList<>();
        if (resumeSubmissions != null) {
            for (ResumeSubmission submission : resumeSubmissions) {
                if (submission != null && submission.getCourse() != null) {
                    addOrUpdateApplication(
                            submission.getCourse(),
                            submission.getApplicationFormId(),
                            submission.getStatus(),
                            submission.isReviewUnread());
                }
            }
        }
    }

    /**
     * Adds a course to the TA's list of applied classes if it is not already present.
     *
     * @param course the course to add
     */
    public void addClass(Course course) {
        if (course != null && !appliedClasses.contains(course)) {
            appliedClasses.add(course);
        }
    }

    /**
     * Adds or updates a resume submission with default pending status.
     *
     * @param course          the course associated with the resume
     * @param resumeDirectory the resume directory path or application form identifier
     */
    public void addOrUpdateResume(Course course, String resumeDirectory) {
        addOrUpdateApplication(course, resumeDirectory, ResumeSubmission.STATUS_PENDING, false);
    }

    /**
     * Adds or updates a resume submission with the specified status.
     *
     * @param course          the course associated with the resume
     * @param resumeDirectory the resume directory path or application form identifier
     * @param status          the submission status (e.g., pending, approved, rejected)
     */
    public void addOrUpdateResume(Course course, String resumeDirectory, int status) {
        addOrUpdateApplication(course, resumeDirectory, status, false);
    }

    /**
     * Adds or updates a resume submission with the specified status and unread flag.
     *
     * @param course          the course associated with the resume
     * @param resumeDirectory the resume directory path or application form identifier
     * @param status          the submission status
     * @param reviewUnread    whether the review result has been viewed by the TA
     */
    public void addOrUpdateResume(Course course, String resumeDirectory, int status, boolean reviewUnread) {
        addOrUpdateApplication(course, resumeDirectory, status, reviewUnread);
    }

    /**
     * Adds or updates an application with default pending status.
     *
     * @param course            the course to apply for
     * @param applicationFormId the application form identifier
     */
    public void addOrUpdateApplication(Course course, String applicationFormId) {
        addOrUpdateApplication(course, applicationFormId, ResumeSubmission.STATUS_PENDING, false);
    }

    /**
     * Adds or updates an application with the specified status.
     *
     * @param course            the course to apply for
     * @param applicationFormId the application form identifier
     * @param status            the application status
     */
    public void addOrUpdateApplication(Course course, String applicationFormId, int status) {
        addOrUpdateApplication(course, applicationFormId, status, false);
    }

    /**
     * Adds or updates an application for a course. If a submission for the given
     * course already exists, its fields are updated; otherwise a new
     * {@link ResumeSubmission} is created.
     *
     * @param course            the course to apply for
     * @param applicationFormId the application form identifier
     * @param status            the application status
     * @param reviewUnread      whether the review result is unread
     */
    public void addOrUpdateApplication(Course course, String applicationFormId, int status, boolean reviewUnread) {
        if (course == null || applicationFormId == null || applicationFormId.isBlank()) {
            return;
        }

        addClass(course);

        for (ResumeSubmission submission : resumeSubmissions) {
            if (course.equals(submission.getCourse())) {
                submission.setCourse(course);
                submission.setApplicationFormId(applicationFormId);
                submission.setStatus(status);
                submission.setReviewUnread(reviewUnread);
                return;
            }
        }

        resumeSubmissions.add(new ResumeSubmission(course, applicationFormId, status, reviewUnread));
    }

    /**
     * Returns the resume directory path for the specified course.
     * <p>This is an alias for {@link #getApplicationFormIdForCourse(String)}.</p>
     *
     * @param courseId the unique identifier of the course
     * @return the resume directory path, or {@code null} if not found
     */
    public String getResumeDirectoryForCourse(String courseId) {
        return getApplicationFormIdForCourse(courseId);
    }

    /**
     * Returns the application form identifier for the specified course.
     *
     * @param courseId the unique identifier of the course
     * @return the application form identifier, or {@code null} if not found
     */
    public String getApplicationFormIdForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        for (ResumeSubmission submission : resumeSubmissions) {
            Course course = submission.getCourse();
            if (course != null && courseId.equals(course.getId())) {
                return submission.getApplicationFormId();
            }
        }
        return null;
    }

    /**
     * Withdraws the TA's application from the specified course.
     * <p>Removes the course from the applied classes list and removes all
     * associated resume submissions.</p>
     *
     * @param courseId the unique identifier of the course to withdraw from
     */
    public void withdrawApplication(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return;
        }

        appliedClasses.removeIf(course -> course != null && Objects.equals(course.getId(), courseId));

        Iterator<ResumeSubmission> iterator = resumeSubmissions.iterator();
        while (iterator.hasNext()) {
            ResumeSubmission submission = iterator.next();
            Course course = submission.getCourse();
            if (course != null && Objects.equals(course.getId(), courseId)) {
                iterator.remove();
            }
        }
    }

    /**
     * Returns the review status of the resume for the specified course.
     *
     * @param courseId the unique identifier of the course
     * @return the status code (e.g., {@link ResumeSubmission#STATUS_PENDING},
     *         {@link ResumeSubmission#STATUS_APPROVED},
     *         {@link ResumeSubmission#STATUS_REJECTED}), or {@code null}
     *         if no submission exists for the course
     */
    public Integer getResumeStatusForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        ResumeSubmission submission = findSubmissionByCourseId(courseId);
        if (submission != null) {
            return submission.getStatus();
        }
        return null;
    }

    /**
     * Checks whether the TA has any unread review updates across all submissions.
     *
     * @return {@code true} if at least one submission has an unread review update,
     *         {@code false} otherwise
     */
    public boolean hasUnreadReviewUpdates() {
        for (ResumeSubmission submission : resumeSubmissions) {
            if (submission != null && submission.isReviewUnread()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the review result for a specific course is unread.
     *
     * @param courseId the unique identifier of the course
     * @return {@code true} if the review update is unread for the course,
     *         {@code false} otherwise
     */
    public boolean isReviewUnreadForCourse(String courseId) {
        ResumeSubmission submission = findSubmissionByCourseId(courseId);
        return submission != null && submission.isReviewUnread();
    }

    /**
     * Marks the review update for the specified course as unread.
     *
     * @param courseId the unique identifier of the course
     */
    public void markReviewUpdateUnread(String courseId) {
        ResumeSubmission submission = findSubmissionByCourseId(courseId);
        if (submission != null) {
            submission.setReviewUnread(true);
        }
    }

    /**
     * Marks the review update for the specified course as read.
     *
     * @param courseId the unique identifier of the course
     * @return {@code true} if the review update was marked as read,
     *         {@code false} if no unread update existed
     */
    public boolean markReviewUpdateRead(String courseId) {
        ResumeSubmission submission = findSubmissionByCourseId(courseId);
        if (submission != null && submission.isReviewUnread()) {
            submission.setReviewUnread(false);
            return true;
        }
        return false;
    }

    /**
     * Marks all review updates across all submissions as read.
     *
     * @return {@code true} if at least one submission was changed from unread to read,
     *         {@code false} otherwise
     */
    public boolean markAllReviewUpdatesRead() {
        boolean changed = false;
        for (ResumeSubmission submission : resumeSubmissions) {
            if (submission != null && submission.isReviewUnread()) {
                submission.setReviewUnread(false);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Finds a resume submission by course identifier.
     *
     * @param courseId the unique identifier of the course to search for
     * @return the matching {@link ResumeSubmission}, or {@code null} if not found
     */
    private ResumeSubmission findSubmissionByCourseId(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        for (ResumeSubmission submission : resumeSubmissions) {
            Course course = submission.getCourse();
            if (course != null && courseId.equals(course.getId())) {
                return submission;
            }
        }
        return null;
    }
}
