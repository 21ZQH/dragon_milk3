package model;

import java.util.ArrayList;
import java.util.List;

public class TA extends User {
    private String role = "TA";
    private List<Course> appliedClasses = new ArrayList<>();
    private List<ResumeSubmission> resumeSubmissions = new ArrayList<>();

    public TA(String password, String email) {
        super(password, email);
    }

    @Override
    public String getRole() {
        return role;
    }

    public List<Course> getAppliedClasses() {
        return appliedClasses;
    }

    public void setAppliedClasses(List<Course> appliedClasses) {
        this.appliedClasses = new ArrayList<>();
        if (appliedClasses != null) {
            for (Course course : appliedClasses) {
                addClass(course);
            }
        }
    }

    public List<ResumeSubmission> getResumeSubmissions() {
        return resumeSubmissions;
    }

    public void setResumeSubmissions(List<ResumeSubmission> resumeSubmissions) {
        this.resumeSubmissions = new ArrayList<>();
        if (resumeSubmissions != null) {
            for (ResumeSubmission submission : resumeSubmissions) {
                if (submission != null && submission.getCourse() != null) {
                    addOrUpdateResume(submission.getCourse(), submission.getResumeDirectory());
                }
            }
        }
    }

    public void addClass(Course course) {
        if (course != null && !appliedClasses.contains(course)) {
            appliedClasses.add(course);
        }
    }

    public void addOrUpdateResume(Course course, String resumeDirectory) {
        if (course == null || resumeDirectory == null || resumeDirectory.isBlank()) {
            return;
        }

        addClass(course);

        for (ResumeSubmission submission : resumeSubmissions) {
            if (course.equals(submission.getCourse())) {
                submission.setCourse(course);
                submission.setResumeDirectory(resumeDirectory);
                return;
            }
        }

        resumeSubmissions.add(new ResumeSubmission(course, resumeDirectory));
    }

    public String getResumeDirectoryForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        for (ResumeSubmission submission : resumeSubmissions) {
            Course course = submission.getCourse();
            if (course != null && courseId.equals(course.getId())) {
                return submission.getResumeDirectory();
            }
        }
        return null;
    }
}
