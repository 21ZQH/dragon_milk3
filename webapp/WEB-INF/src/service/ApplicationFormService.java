package service;

import model.ApplicationForm;
import model.Course;
import model.TA;

public interface ApplicationFormService {
    ApplicationForm generateInitialForm(TA ta, Course course);

    ApplicationForm getForm(String taEmail, String courseId);

    ApplicationForm buildFormFromRequest(TA ta, Course course,
            String applicantName, String email, String education, String skills,
            String relevantExperience, String projectExperience, String feedback, boolean submitted);

    void saveForm(ApplicationForm form);
}
