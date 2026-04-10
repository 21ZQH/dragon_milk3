package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Course {
    private final String id;
    private String courseName;
    private String jobTitle;
    private String workingHours;
    private String salary;
    private String jobDescription;
    private String jobRequirement;
    private List<TA> taApplicants;
    private List<String> applicantResumes;

    public Course(String courseName, String jobTitle, String workingHours, String salary, String jobDescription, String jobRequirement) {
        this(UUID.randomUUID().toString(), courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
    }

    public Course(String id, String courseName, String jobTitle, String workingHours, String salary, String jobDescription, String jobRequirement) {
        this.id = id;
        this.courseName = courseName;
        this.jobTitle = jobTitle;
        this.workingHours = workingHours;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.jobRequirement = jobRequirement;
        this.taApplicants = new ArrayList<>();
        this.applicantResumes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobRequirement() {
        return jobRequirement;
    }

    public void setJobRequirement(String jobRequirement) {
        this.jobRequirement = jobRequirement;
    }

    public List<TA> getTaApplicants() {
        return taApplicants;
    }

    public List<String> getApplicantResumes() {
        return applicantResumes;
    }

    public void addApplication(TA ta, String resumeDirectory) {
        for (int i = 0; i < taApplicants.size(); i++) {
            TA existingApplicant = taApplicants.get(i);
            if (existingApplicant != null && ta != null && Objects.equals(existingApplicant.getEmail(), ta.getEmail())) {
                taApplicants.set(i, ta);
                applicantResumes.set(i, resumeDirectory);
                return;
            }
        }

        taApplicants.add(ta);
        applicantResumes.add(resumeDirectory);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Course course)) {
            return false;
        }
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
