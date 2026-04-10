package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TA extends User {
    private String role = "TA";
    private String college;
    private String skill;
    private List<Course> appliedClasses = new ArrayList<>();
    private List<ResumeSubmission> resumeSubmissions = new ArrayList<>();

    public TA(String password, String email) {
        super(password, email);
    }

    @Override
    public String getRole() {
        return role;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
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
                    addOrUpdateResume(submission.getCourse(), submission.getResumeDirectory(), submission.getStatus());
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
        addOrUpdateResume(course, resumeDirectory, ResumeSubmission.STATUS_PENDING);
    }

    public void addOrUpdateResume(Course course, String resumeDirectory, int status) {
        if (course == null || resumeDirectory == null || resumeDirectory.isBlank()) {
            return;
        }

        addClass(course);

        for (ResumeSubmission submission : resumeSubmissions) {
            if (course.equals(submission.getCourse())) {
                submission.setCourse(course);
                submission.setResumeDirectory(resumeDirectory);
                submission.setStatus(status);
                return;
            }
        }

        resumeSubmissions.add(new ResumeSubmission(course, resumeDirectory, status));
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

    public Integer getResumeStatusForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return null;
        }

        for (ResumeSubmission submission : resumeSubmissions) {
            Course course = submission.getCourse();
            if (course != null && courseId.equals(course.getId())) {
                return submission.getStatus();
            }
        }
        return null;
    }
}
