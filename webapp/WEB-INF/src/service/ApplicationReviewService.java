package service;

import java.util.Map;

import model.ApplicationForm;
import model.Course;

/**
 * Service interface for managing the review of TA applications by Module Officers.
 * Provides methods for retrieving submitted application forms, saving review picks,
 * and publishing review results for courses.
 *
 * @version 1.0
 * @since 2025
 */
public interface ApplicationReviewService {
    /**
     * Retrieves all submitted application forms for a given course,
     * keyed by the applicant's email address.
     *
     * @param course the course for which to retrieve submitted forms
     * @return a map of applicant email to {@link ApplicationForm}
     *         containing all submitted forms for the course
     */
    Map<String, ApplicationForm> getSubmittedFormsByApplicantEmail(Course course);

    /**
     * Saves the MO's selected picks of applicants for a given course.
     *
     * @param course        the course being reviewed
     * @param pickedEmails  an array of email addresses of the picked applicants
     * @return {@code true} if the picks were saved, {@code false} otherwise
     */
    boolean saveReviewPicks(Course course, String[] pickedEmails);

    /**
     * Publishes the review results for a course, making the selected
     * applicants visible to all involved parties.
     *
     * @param course        the course for which to publish results
     * @param pickedEmails  an array of email addresses of the selected applicants
     * @return {@code true} if the review was published, {@code false} otherwise
     */
    boolean publishReview(Course course, String[] pickedEmails);
}
