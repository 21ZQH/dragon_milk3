package repository;

import model.ApplicationForm;

public interface ApplicationFormRepository {
    ApplicationForm findForm(String taEmail, String courseId);

    void saveOrUpdate(ApplicationForm form);
}
