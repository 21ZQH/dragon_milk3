package repository;

import model.ApplicationForm;

/**
 * Data access boundary for TA application forms.
 *
 * <p>The service layer uses this interface instead of calling the text-file
 * store directly. This keeps the project architecture stable:
 * controller -> service -> repository -> store -> text file.</p>
 */
public interface ApplicationFormRepository {
    /**
     * Finds the saved form for one TA and one course.
     *
     * @param taEmail TA account email used as the applicant identity
     * @param courseId unique course identifier
     * @return the matching form, or {@code null} when no form has been saved
     */
    ApplicationForm findForm(String taEmail, String courseId);

    /**
     * Creates a new form record or replaces the existing record for the same
     * TA email and course id.
     *
     * @param form application form to persist; invalid forms are ignored by
     *             the underlying store
     */
    void saveOrUpdate(ApplicationForm form);
}
