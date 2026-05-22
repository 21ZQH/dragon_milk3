package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Course recruitment project managed by an MO.
 *
 * <p>The model stores both course information and recruitment workflow state:
 * draft/published status, TA applicants, submitted application form ids,
 * picked applicant emails, and whether final review results have been
 * published.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class Course {
    /** The unique identifier for this course. */
    private final String id;
    /** The name of the course. */
    private String courseName;
    /** The job title for the TA position. */
    private String jobTitle;
    /** The expected working hours for the TA position. */
    private String workingHours;
    /** The salary or compensation for the TA position. */
    private String salary;
    /** The description of the TA job responsibilities. */
    private String jobDescription;
    /** The requirements for the TA position. */
    private String jobRequirement;
    /** The list of TA applicants who have applied to this course. */
    private List<TA> taApplicants;
    /** The list of application form identifiers associated with each applicant. */
    private List<String> applicantFormIds;
    /** The list of email addresses of applicants who have been picked/selected. */
    private List<String> pickedApplicantEmails;
    /** Whether the final review results have been published to TAs. */
    private boolean reviewPublished;
    /** Whether the recruitment information has been published and made visible to TAs. */
    private boolean recruitmentPublished;

    /**
     * Constructs a new Course with an auto-generated unique identifier.
     *
     * @param courseName       the name of the course
     * @param jobTitle         the job title for the TA position
     * @param workingHours     the expected working hours
     * @param salary           the salary or compensation
     * @param jobDescription   the job description
     * @param jobRequirement   the job requirements
     */
    public Course(String courseName, String jobTitle, String workingHours, String salary, String jobDescription, String jobRequirement) {
        this(UUID.randomUUID().toString(), courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
    }

    /**
     * Constructs a Course with a specified identifier.
     *
     * @param id               the unique identifier for the course
     * @param courseName       the name of the course
     * @param jobTitle         the job title for the TA position
     * @param workingHours     the expected working hours
     * @param salary           the salary or compensation
     * @param jobDescription   the job description
     * @param jobRequirement   the job requirements
     */
    public Course(String id, String courseName, String jobTitle, String workingHours, String salary, String jobDescription, String jobRequirement) {
        this.id = id;
        this.courseName = courseName;
        this.jobTitle = jobTitle;
        this.workingHours = workingHours;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.jobRequirement = jobRequirement;
        this.taApplicants = new ArrayList<>();
        this.applicantFormIds = new ArrayList<>();
        this.pickedApplicantEmails = new ArrayList<>();
        this.reviewPublished = false;
        this.recruitmentPublished = false;
    }

    /**
     * Returns the unique identifier of the course.
     *
     * @return the course identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the course.
     *
     * @return the course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Sets the name of the course.
     *
     * @param courseName the course name to set
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Returns the job title for the TA position.
     *
     * @return the job title
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the job title for the TA position.
     *
     * @param jobTitle the job title to set
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Returns the expected working hours for the TA position.
     *
     * @return the working hours
     */
    public String getWorkingHours() {
        return workingHours;
    }

    /**
     * Sets the expected working hours for the TA position.
     *
     * @param workingHours the working hours to set
     */
    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    /**
     * Returns the salary or compensation for the TA position.
     *
     * @return the salary
     */
    public String getSalary() {
        return salary;
    }

    /**
     * Sets the salary or compensation for the TA position.
     *
     * @param salary the salary to set
     */
    public void setSalary(String salary) {
        this.salary = salary;
    }

    /**
     * Returns the description of the TA job responsibilities.
     *
     * @return the job description
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * Sets the description of the TA job responsibilities.
     *
     * @param jobDescription the job description to set
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * Returns the requirements for the TA position.
     *
     * @return the job requirements
     */
    public String getJobRequirement() {
        return jobRequirement;
    }

    /**
     * Sets the requirements for the TA position.
     *
     * @param jobRequirement the job requirements to set
     */
    public void setJobRequirement(String jobRequirement) {
        this.jobRequirement = jobRequirement;
    }

    /**
     * Returns the list of TA applicants for this course.
     *
     * @return the list of TA applicants
     */
    public List<TA> getTaApplicants() {
        return taApplicants;
    }

    /**
     * Returns the list of application form identifiers associated with each applicant.
     *
     * @return the list of application form identifiers
     */
    public List<String> getApplicantFormIds() {
        return applicantFormIds;
    }

    /**
     * Returns a copy of the list of email addresses of picked/selected applicants.
     *
     * @return a new list containing the picked applicant emails
     */
    public List<String> getPickedApplicantEmails() {
        return new ArrayList<>(pickedApplicantEmails);
    }

    /**
     * Sets the list of picked applicant email addresses.
     * <p>Duplicate and blank emails are filtered out during population.</p>
     *
     * @param pickedApplicantEmails the collection of email addresses to set
     */
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

    /**
     * Checks whether a specific applicant has been picked/selected.
     *
     * @param email the email address of the applicant
     * @return {@code true} if the applicant has been picked, {@code false} otherwise
     */
    public boolean isApplicantPicked(String email) {
        return email != null && pickedApplicantEmails.contains(email);
    }

    /**
     * Checks whether the final review results have been published.
     *
     * @return {@code true} if reviews are published, {@code false} otherwise
     */
    public boolean isReviewPublished() {
        return reviewPublished;
    }

    /**
     * Sets whether the final review results have been published.
     *
     * @param reviewPublished {@code true} to publish review results, {@code false} otherwise
     */
    public void setReviewPublished(boolean reviewPublished) {
        this.reviewPublished = reviewPublished;
    }

    /**
     * Checks whether the recruitment information has been published.
     *
     * @return {@code true} if recruitment is published, {@code false} otherwise
     */
    public boolean isRecruitmentPublished() {
        return recruitmentPublished;
    }

    /**
     * Sets whether the recruitment information has been published.
     *
     * @param recruitmentPublished {@code true} to publish recruitment, {@code false} otherwise
     */
    public void setRecruitmentPublished(boolean recruitmentPublished) {
        this.recruitmentPublished = recruitmentPublished;
    }

    /**
     * Adds or updates a TA application for this course.
     * <p>If the TA has already applied (matched by email), the existing entry is
     * updated; otherwise a new application entry is created.</p>
     *
     * @param ta                the TA applicant
     * @param applicationFormId the application form identifier associated with this application
     */
    public void addApplication(TA ta, String applicationFormId) {
        for (int i = 0; i < taApplicants.size(); i++) {
            TA existingApplicant = taApplicants.get(i);
            if (existingApplicant != null && ta != null && Objects.equals(existingApplicant.getEmail(), ta.getEmail())) {
                taApplicants.set(i, ta);
                applicantFormIds.set(i, applicationFormId);
                return;
            }
        }

        taApplicants.add(ta);
        applicantFormIds.add(applicationFormId);
    }

    /**
     * Removes a TA application by the applicant's email address.
     *
     * @param taEmail the email address of the TA whose application should be removed
     */
    public void removeApplicationByTaEmail(String taEmail) {
        if (taEmail == null || taEmail.isBlank()) {
            return;
        }

        for (int i = taApplicants.size() - 1; i >= 0; i--) {
            TA applicant = taApplicants.get(i);
            if (applicant != null && Objects.equals(taEmail, applicant.getEmail())) {
                taApplicants.remove(i);
                if (i < applicantFormIds.size()) {
                    applicantFormIds.remove(i);
                }
            }
        }
    }

    /**
     * Compares this course to another object for equality based on the course identifier.
     *
     * @param other the object to compare with
     * @return {@code true} if the objects are equal or share the same course id,
     *         {@code false} otherwise
     */
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

    /**
     * Returns a hash code for this course based on its identifier.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
