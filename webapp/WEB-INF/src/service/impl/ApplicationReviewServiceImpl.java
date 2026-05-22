package service.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import model.ApplicationForm;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import repository.ApplicationFormRepository;
import repository.CourseRepository;
import repository.UserRepository;
import repository.impl.TxtApplicationFormRepositoryImpl;
import repository.impl.TxtCourseRepositoryImpl;
import repository.impl.TxtUserRepositoryImpl;
import service.ApplicationReviewService;

/**
 * Implementation of the {@link ApplicationReviewService} interface.
 * Provides concrete logic for reviewing TA applications, including
 * retrieving submitted forms, saving and publishing review picks, and
 * updating applicant statuses (approved/rejected). Delegates data
 * access to {@link UserRepository}, {@link ApplicationFormRepository},
 * and {@link CourseRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class ApplicationReviewServiceImpl implements ApplicationReviewService {
    /** Repository for user persistence operations. */
    private final UserRepository userRepository;

    /** Repository for application form persistence. */
    private final ApplicationFormRepository applicationFormRepository;

    /** Repository for course persistence operations. */
    private final CourseRepository courseRepository;

    /**
     * Constructs a new {@code ApplicationReviewServiceImpl} with default
     * {@link TxtUserRepositoryImpl}, {@link TxtApplicationFormRepositoryImpl},
     * and {@link TxtCourseRepositoryImpl}.
     */
    public ApplicationReviewServiceImpl() {
        this(new TxtUserRepositoryImpl(), new TxtApplicationFormRepositoryImpl(), new TxtCourseRepositoryImpl());
    }

    /**
     * Constructs a new {@code ApplicationReviewServiceImpl} with the given
     * repositories.
     *
     * @param userRepository             the repository for user data access
     * @param applicationFormRepository  the repository for application form data access
     * @param courseRepository           the repository for course data access
     */
    ApplicationReviewServiceImpl(UserRepository userRepository,
            ApplicationFormRepository applicationFormRepository,
            CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.applicationFormRepository = applicationFormRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Retrieves a map of submitted application forms keyed by applicant email
     * for the given course.
     *
     * @param course the course whose submitted forms are to be retrieved
     * @return a {@link Map} of applicant email to {@link ApplicationForm},
     *         containing only submitted forms; never {@code null}
     */
    @Override
    public Map<String, ApplicationForm> getSubmittedFormsByApplicantEmail(Course course) {
        Map<String, ApplicationForm> formsByEmail = new LinkedHashMap<>();
        if (course == null) {
            return formsByEmail;
        }

        for (TA applicant : course.getTaApplicants()) {
            if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()) {
                continue;
            }

            ApplicationForm form = applicationFormRepository.findForm(applicant.getEmail(), course.getId());
            if (form != null && form.isSubmitted()) {
                formsByEmail.put(applicant.getEmail(), form);
            }
        }
        return formsByEmail;
    }

    /**
     * Saves the MO's review picks for a course without publishing them.
     *
     * @param course        the course whose picks are being saved
     * @param pickedEmails  an array of applicant email addresses that were picked
     */
    @Override
    public void saveReviewPicks(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        courseRepository.updateCourse(course);
    }

    /**
     * Publishes the review results for a course, setting the picked applicants
     * and updating the status of all submitted applications accordingly.
     *
     * @param course        the course whose review is being published
     * @param pickedEmails  an array of applicant email addresses that were picked
     */
    @Override
    public void publishReview(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        applyPublishedStatuses(course);
        course.setReviewPublished(true);
        courseRepository.updateCourse(course);
    }

    /**
     * Resolves the set of valid picked applicant emails, filtering against
     * the set of applicants who have submitted forms for the course.
     *
     * @param course        the course being reviewed
     * @param pickedEmails  an array of candidate email addresses
     * @return a {@link Set} of valid picked applicant email addresses
     */
    private Set<String> resolvePickedApplicantEmails(Course course, String[] pickedEmails) {
        Set<String> validApplicantEmails = getSubmittedFormsByApplicantEmail(course).keySet();

        Set<String> resolvedPickedEmails = new LinkedHashSet<>();
        if (pickedEmails == null) {
            return resolvedPickedEmails;
        }

        for (String pickedEmail : pickedEmails) {
            if (pickedEmail != null && validApplicantEmails.contains(pickedEmail)) {
                resolvedPickedEmails.add(pickedEmail);
            }
        }
        return resolvedPickedEmails;
    }

    /**
     * Applies the approved or rejected status to all submitted applications
     * for the given course based on the MO's picks.
     *
     * @param course the course whose statuses are being applied
     */
    private void applyPublishedStatuses(Course course) {
        Map<String, ApplicationForm> submittedFormsByEmail = getSubmittedFormsByApplicantEmail(course);
        for (TA applicant : course.getTaApplicants()) {
            if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()) {
                continue;
            }

            if (!submittedFormsByEmail.containsKey(applicant.getEmail())) {
                continue;
            }

            String applicationFormId = applicant.getApplicationFormIdForCourse(course.getId());
            if (applicationFormId == null || applicationFormId.isBlank()) {
                continue;
            }

            int status = course.isApplicantPicked(applicant.getEmail())
                    ? ResumeSubmission.STATUS_APPROVED
                    : ResumeSubmission.STATUS_REJECTED;
            applicant.addOrUpdateApplication(course, applicationFormId, status, true);
            course.addApplication(applicant, applicationFormId);
            userRepository.updateAppliedCourseIds(applicant);
        }
    }
}
