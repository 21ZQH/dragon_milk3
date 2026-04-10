package model;

import java.util.ArrayList;
import java.util.Collection;
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
    private List<String> pickedApplicantEmails;
    private boolean reviewPublished;

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
        this.pickedApplicantEmails = new ArrayList<>();
        this.reviewPublished = false;
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

    public List<String> getPickedApplicantEmails() {
        return new ArrayList<>(pickedApplicantEmails);
    }

    public void setPickedApplicantEmails(Collection<String> pickedApplicantEmails) {
        this.pickedApplicantEmails = new ArrayList<>();
        if (pickedApplicantEmails == null) {
            return;
        }

        for (String email : pickedApplicantEmails) {
            if (email == null || email.isBlank() || this.pickedApplicantEmails.contains(email)) {
                continue;
            }
            this.pickedApplicantEmails.add(email);
        }
    }

    public boolean isApplicantPicked(String email) {
        return email != null && pickedApplicantEmails.contains(email);
    }

    public boolean isReviewPublished() {
        return reviewPublished;
    }

    public void setReviewPublished(boolean reviewPublished) {
        this.reviewPublished = reviewPublished;
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

    public void removeApplicationByTaEmail(String taEmail) {
        if (taEmail == null || taEmail.isBlank()) {
            return;
        }

        for (int i = taApplicants.size() - 1; i >= 0; i--) {
            TA applicant = taApplicants.get(i);
            if (applicant != null && Objects.equals(taEmail, applicant.getEmail())) {
                taApplicants.remove(i);
                if (i < applicantResumes.size()) {
                    applicantResumes.remove(i);
                }
            }
        }
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
