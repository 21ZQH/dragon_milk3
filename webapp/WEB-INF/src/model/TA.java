package model;

import java.util.ArrayList;
import java.util.List;

public class TA extends User {
    private String role = "TA";
    private List<String> resumeNames = new ArrayList<>();
    private List<String> resumes = new ArrayList<>();
    private List<Course> appliedClasses = new ArrayList<>();

    public TA(String password, String email) {
        super(password, email);
    }

    @Override
    public String getRole() {
        return role;
    }

    public List<String> getResumeNames() {
        return resumeNames;
    }

    //非必要
    public void setResumeNames(List<String> resumeNames) {
        this.resumeNames = (resumeNames == null) ? new ArrayList<>() : resumeNames;
    }

    public List<String> getResumes() {
        return resumes;
    }

    public List<Course> getAppliedClasses() {
        return appliedClasses;
    }

    public void setAppliedClasses(List<Course> appliedClasses) {
        this.appliedClasses = (appliedClasses == null) ? new ArrayList<>() : appliedClasses;
    }

    public void setResumes(List<String> resumes) {
        this.resumes = (resumes == null) ? new ArrayList<>() : resumes;
    }

    public void addResume(String resumeName, String resume) {
        resumeNames.add(resumeName);
        resumes.add(resume);
    }

    public String getResumeByIndex(int index) {
        if (index < 0 || index >= resumes.size()) {
            return null;
        }
        return resumes.get(index);
    }

    public void addClass(Course course) {
        if (course != null) {
            appliedClasses.add(course);
        }
    }

}