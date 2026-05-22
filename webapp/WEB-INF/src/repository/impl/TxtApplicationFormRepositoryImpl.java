package repository.impl;

import model.ApplicationForm;
import repository.ApplicationFormRepository;
import store.ApplicationFormStore;

/**
 * Text-file implementation of {@link ApplicationFormRepository}.
 *
 * <p>This repository is intentionally thin. It preserves the repository
 * abstraction while delegating actual serialization and file handling to
 * {@link ApplicationFormStore}. Application forms are stored in
 * {@code WEB-INF/file/application-forms.txt} using tab-separated records with
 * Base64-encoded text fields.</p>
 */
public class TxtApplicationFormRepositoryImpl implements ApplicationFormRepository {
    /**
     * Delegates lookup to {@link ApplicationFormStore#findForm(String, String)}.
     *
     * @param taEmail TA account email
     * @param courseId course identifier
     * @return the persisted form, or {@code null} if no record exists
     */
    @Override
    public ApplicationForm findForm(String taEmail, String courseId) {
        return ApplicationFormStore.findForm(taEmail, courseId);
    }

    /**
     * Delegates create/update persistence to {@link ApplicationFormStore}.
     *
     * @param form form to save
     */
    @Override
    public void saveOrUpdate(ApplicationForm form) {
        ApplicationFormStore.saveOrUpdate(form);
    }
}
