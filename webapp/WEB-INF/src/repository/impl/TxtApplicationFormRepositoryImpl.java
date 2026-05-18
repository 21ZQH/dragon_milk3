package repository.impl;

import model.ApplicationForm;
import repository.ApplicationFormRepository;
import store.ApplicationFormStore;

public class TxtApplicationFormRepositoryImpl implements ApplicationFormRepository {
    @Override
    public ApplicationForm findForm(String taEmail, String courseId) {
        return ApplicationFormStore.findForm(taEmail, courseId);
    }

    @Override
    public void saveOrUpdate(ApplicationForm form) {
        ApplicationFormStore.saveOrUpdate(form);
    }
}
