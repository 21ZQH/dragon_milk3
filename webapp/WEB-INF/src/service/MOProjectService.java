package service;

import java.util.List;

import model.Course;
import model.Mo;

/**
 * Service interface for Module Officer (MO) project management within
 * the TA recruitment system. Provides methods for publishing courses,
 * saving drafts, updating course details, managing MO profiles, and
 * handling the review process including picking and publishing
 * selected applicants.
 *
 * @version 1.0
 * @since 2025
 */
public interface MOProjectService {
    /**
     * Publishes a new course with the specified details and associates it with the MO.
     *
     * @param mo              the Module Officer publishing the course
     * @param courseName      the name of the course
     * @param jobTitle        the job title for the TA position
     * @param workingHours    the expected weekly working hours
     * @param jobDescription  a description of the job responsibilities
     * @param jobRequirement  the qualifications and requirements for the position
     * @return the published {@link Course} object
     */
    Course publishCourse(Mo mo, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement);

    /**
     * Saves a course as a draft without publishing it. Used for creating
     * new course entries that are not yet ready for publication.
     *
     * @param mo              the Module Officer saving the draft
     * @param oldCourse       the previous version of the course, or {@code null} if new
     * @param courseName      the name of the course
     * @param jobTitle        the job title for the TA position
     * @param jobDescription  a description of the job responsibilities
     * @param jobRequirement  the qualifications and requirements for the position
     * @return the saved draft {@link Course} object
     */
    Course saveCourseDraft(Mo mo, Course oldCourse, String courseName, String jobTitle,
            String jobDescription, String jobRequirement);

    /**
     * Updates an existing published course with new details.
     *
     * @param mo              the Module Officer updating the course
     * @param oldCourse       the existing course to be updated
     * @param courseName      the updated course name
     * @param jobTitle        the updated job title
     * @param workingHours    the updated weekly working hours
     * @param jobDescription  the updated job description
     * @param jobRequirement  the updated job requirements
     * @return the updated {@link Course} object
     */
    Course updateCourse(Mo mo, Course oldCourse, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement);

    /**
     * Updates the profile information for a Module Officer.
     *
     * @param mo      the MO whose profile to update
     * @param name    the updated name
     * @param degree  the updated degree information
     * @param college the updated college information
     */
    void updateProfile(Mo mo, String name, String degree, String college);

    /**
     * Refreshes the list of courses owned by the specified MO from the data store.
     *
     * @param mo the MO whose owned courses to refresh
     * @return an updated list of {@link Course} objects owned by the MO
     */
    List<Course> refreshOwnedCourses(Mo mo);

    /**
     * Synchronises a specific course into the MO's owned course list.
     *
     * @param mo     the MO to sync the course with
     * @param course the course to add or update in the MO's owned list
     */
    void syncOwnedCourse(Mo mo, Course course);

    /**
     * Retrieves a course from the given list by its index string parameter.
     *
     * @param courseList        the list of courses to search
     * @param courseIndexParam  the course index as a string parameter
     * @return the matching {@link Course}, or {@code null} if not found
     */
    Course getCourseByIndex(List<Course> courseList, String courseIndexParam);

    /**
     * Resolves the course index for review purposes from a string parameter.
     *
     * @param courseIndexParam  the course index as a string parameter
     * @param courseList        the list of courses to search
     * @return the zero-based index of the course in the list
     */
    int resolveCourseIndexForReview(String courseIndexParam, List<Course> courseList);

    /**
     * Resolves the email addresses of picked applicants from the raw selection array.
     *
     * @param course        the course for which applicants were picked
     * @param pickedEmails  an array of picked applicant identifiers
     * @return a list of resolved applicant email addresses
     */
    List<String> resolvePickedApplicantEmails(Course course, String[] pickedEmails);

    /**
     * Saves the MO's selected review picks for a course.
     *
     * @param course        the course being reviewed
     * @param pickedEmails  an array of email addresses of the selected applicants
     * @return {@code true} if the picks were saved successfully, {@code false} otherwise
     */
    boolean saveReviewPicks(Course course, String[] pickedEmails);

    /**
     * Publishes the review results for a course, making the selected
     * applicants visible to all parties.
     *
     * @param course        the course for which to publish results
     * @param pickedEmails  an array of email addresses of the selected applicants
     */
    void publishReview(Course course, String[] pickedEmails);
}
