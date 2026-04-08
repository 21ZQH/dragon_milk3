package model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseName;
    private String jobTitle;
    private String workingHours;
    private String salary;
    
    // Additional fields for posting details
    private String jobDescription;
    private String jobRequirement;
    private List<TA> taApplicants;
    private List<String> applicantResumes;
   
    public Course(String courseName, String jobTitle, String workingHours, String salary, String jobDescription, String jobRequirement) {
        this.courseName = courseName;
        this.jobTitle = jobTitle;
        this.workingHours = workingHours;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.jobRequirement = jobRequirement;
        this.taApplicants = new ArrayList<>();
        this.applicantResumes = new ArrayList<>();
    }

    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getJobRequirement() { return jobRequirement; }
    public void setJobRequirement(String jobRequirement) { this.jobRequirement = jobRequirement; }
    
    public List<TA> getTaApplicants() { return taApplicants; }
    public List<String> getApplicantResumes() { return applicantResumes; }

    public void addApplication(TA ta, String resume) {
        taApplicants.add(ta);
        applicantResumes.add(resume);
    }
}