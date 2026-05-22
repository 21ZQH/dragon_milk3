package service;

import model.ApplicationForm;
import model.Course;
import model.TA;

/**
 * Service interface for managing TA application forms.
 * Provides methods for generating, retrieving, building, and persisting
 * application forms submitted by Teaching Assistants for specific courses.
 *
 * @version 1.0
 * @since 2025
 */
public interface ApplicationFormService {
    /**
     * Generates an initial blank application form for a TA applying to a course.
     *
     * @param ta     the Teaching Assistant submitting the application
     * @param course the course being applied to
     * @return a new {@link ApplicationForm} with default values
     */
    ApplicationForm generateInitialForm(TA ta, Course course);

    /**
     * Retrieves the existing application form submitted by a TA for a specific course.
     *
     * @param taEmail  the email address of the TA
     * @param courseId the identifier of the course
     * @return the existing {@link ApplicationForm}, or {@code null} if none exists
     */
    ApplicationForm getForm(String taEmail, String courseId);

    /**
     * Builds an application form from the submitted request parameters.
     *
     * @param ta                  the TA submitting the form
     * @param course              the course being applied to
     * @param applicantName       the name of the applicant
     * @param email               the email address of the applicant
     * @param education           the educational background information
     * @param skills              the relevant skills description
     * @param relevantExperience  the relevant work or academic experience
     * @param projectExperience   the project experience details
     * @param feedback            additional feedback or comments
     * @param submitted           whether the form is being submitted or saved as draft
     * @return a fully populated {@link ApplicationForm}
     */
    ApplicationForm buildFormFromRequest(TA ta, Course course,
            String applicantName, String email, String education, String skills,
            String relevantExperience, String projectExperience, String feedback, boolean submitted);

    /**
     * Persists the given application form to the data store.
     *
     * @param form the {@link ApplicationForm} to save
     */
    void saveForm(ApplicationForm form);
}
