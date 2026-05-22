package model;

/**
 * Per-course application status owned by a TA.
 *
 * <p>The status tracks whether the application is pending, approved, or
 * rejected. The unread flag supports notification dots after an MO publishes a
 * final review result.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class ResumeSubmission {
    /** Status constant indicating the application is pending review. */
    public static final int STATUS_PENDING = 0;
    /** Status constant indicating the application has been approved. */
    public static final int STATUS_APPROVED = 1;
    /** Status constant indicating the application has been rejected. */
    public static final int STATUS_REJECTED = 2;

    /** The course associated with this submission. */
    private Course course;
    /** The application form identifier for this submission. */
    private String applicationFormId;
    /** The current review status of this submission. */
    private int status;
    /** Whether the review result has not yet been viewed by the TA. */
    private boolean reviewUnread;

    /**
     * Constructs a ResumeSubmission with default pending status.
     *
     * @param course            the course associated with this submission
     * @param applicationFormId the application form identifier
     */
    public ResumeSubmission(Course course, String applicationFormId) {
        this(course, applicationFormId, STATUS_PENDING, false);
    }

    /**
     * Constructs a ResumeSubmission with the specified status.
     *
     * @param course            the course associated with this submission
     * @param applicationFormId the application form identifier
     * @param status            the review status
     */
    public ResumeSubmission(Course course, String applicationFormId, int status) {
        this(course, applicationFormId, status, false);
    }

    /**
     * Constructs a ResumeSubmission with all fields specified.
     *
     * @param course            the course associated with this submission
     * @param applicationFormId the application form identifier
     * @param status            the review status
     * @param reviewUnread      whether the review result is unread
     */
    public ResumeSubmission(Course course, String applicationFormId, int status, boolean reviewUnread) {
        this.course = course;
        this.applicationFormId = applicationFormId;
        this.status = status;
        this.reviewUnread = reviewUnread;
    }

    /**
     * Returns the course associated with this submission.
     *
     * @return the course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Sets the course associated with this submission.
     *
     * @param course the course to set
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * Returns the application form identifier for this submission.
     *
     * @return the application form identifier
     */
    public String getApplicationFormId() {
        return applicationFormId;
    }

    /**
     * Sets the application form identifier for this submission.
     *
     * @param applicationFormId the application form identifier to set
     */
    public void setApplicationFormId(String applicationFormId) {
        this.applicationFormId = applicationFormId;
    }

    /**
     * Returns the current review status of this submission.
     *
     * @return the status code (one of {@link #STATUS_PENDING},
     *         {@link #STATUS_APPROVED}, or {@link #STATUS_REJECTED})
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the review status of this submission.
     *
     * @param status the status code to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Checks whether the review result has not been viewed by the TA.
     *
     * @return {@code true} if the review is unread, {@code false} otherwise
     */
    public boolean isReviewUnread() {
        return reviewUnread;
    }

    /**
     * Sets whether the review result is unread.
     *
     * @param reviewUnread {@code true} to mark as unread, {@code false} to mark as read
     */
    public void setReviewUnread(boolean reviewUnread) {
        this.reviewUnread = reviewUnread;
    }
}
