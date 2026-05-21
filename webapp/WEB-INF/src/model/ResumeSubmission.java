package model;

/**
 * Per-course application status owned by a TA.
 *
 * <p>The status tracks whether the application is pending, approved, or
 * rejected. The unread flag supports notification dots after an MO publishes a
 * final review result.</p>
 */
public class ResumeSubmission {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    private Course course;
    private String applicationFormId;
    private int status;
    private boolean reviewUnread;

    public ResumeSubmission(Course course, String applicationFormId) {
        this(course, applicationFormId, STATUS_PENDING, false);
    }

    public ResumeSubmission(Course course, String applicationFormId, int status) {
        this(course, applicationFormId, status, false);
    }

    public ResumeSubmission(Course course, String applicationFormId, int status, boolean reviewUnread) {
        this.course = course;
        this.applicationFormId = applicationFormId;
        this.status = status;
        this.reviewUnread = reviewUnread;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getApplicationFormId() {
        return applicationFormId;
    }

    public void setApplicationFormId(String applicationFormId) {
        this.applicationFormId = applicationFormId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isReviewUnread() {
        return reviewUnread;
    }

    public void setReviewUnread(boolean reviewUnread) {
        this.reviewUnread = reviewUnread;
    }
}
