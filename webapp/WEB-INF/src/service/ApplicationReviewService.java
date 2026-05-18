package service;

import java.util.Map;

import model.ApplicationForm;
import model.Course;

public interface ApplicationReviewService {
    Map<String, ApplicationForm> getSubmittedFormsByApplicantEmail(Course course);

    void saveReviewPicks(Course course, String[] pickedEmails);

    void publishReview(Course course, String[] pickedEmails);
}
