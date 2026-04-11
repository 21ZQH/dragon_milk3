package model;

public class ResumeSubmission {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    private Course course;
    private String resumeDirectory;
    private int status;
    private boolean reviewUnread;

    public ResumeSubmission(Course course, String resumeDirectory) {
        this(course, resumeDirectory, STATUS_PENDING, false);
    }

    public ResumeSubmission(Course course, String resumeDirectory, int status) {
        this(course, resumeDirectory, status, false);
    }

    public ResumeSubmission(Course course, String resumeDirectory, int status, boolean reviewUnread) {
        this.course = course;
        this.resumeDirectory = resumeDirectory;
        this.status = status;
        this.reviewUnread = reviewUnread;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getResumeDirectory() {
        return resumeDirectory;
    }

    public void setResumeDirectory(String resumeDirectory) {
        this.resumeDirectory = resumeDirectory;
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
