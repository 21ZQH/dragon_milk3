package model;

public class ResumeSubmission {
    private Course course;
    private String resumeDirectory;

    public ResumeSubmission(Course course, String resumeDirectory) {
        this.course = course;
        this.resumeDirectory = resumeDirectory;
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
}
