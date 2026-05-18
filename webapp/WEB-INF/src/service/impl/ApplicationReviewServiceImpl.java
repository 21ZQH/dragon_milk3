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

public class ApplicationReviewServiceImpl implements ApplicationReviewService {
    private final UserRepository userRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final CourseRepository courseRepository;

    public ApplicationReviewServiceImpl() {
        this(new TxtUserRepositoryImpl(), new TxtApplicationFormRepositoryImpl(), new TxtCourseRepositoryImpl());
    }

    ApplicationReviewServiceImpl(UserRepository userRepository,
            ApplicationFormRepository applicationFormRepository,
            CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.applicationFormRepository = applicationFormRepository;
        this.courseRepository = courseRepository;
    }

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

    @Override
    public void saveReviewPicks(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        courseRepository.updateCourse(course);
    }

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
