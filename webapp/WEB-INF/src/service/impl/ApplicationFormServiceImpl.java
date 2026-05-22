package service.impl;

import java.io.File;
import java.io.IOException;

import model.ApplicationForm;
import model.Course;
import model.TA;
import repository.ApplicationFormRepository;
import repository.impl.TxtApplicationFormRepositoryImpl;
import service.ApplicationFormService;
import service.ai.ApplicationFormAiClient;
import service.ai.ApplicationFormAiClientFactory;
import service.ai.ResumeTextExtractor;
import service.ai.impl.MockApplicationFormAiClient;
import service.ai.impl.PdfBoxResumeTextExtractor;

/**
 * Implementation of the {@link ApplicationFormService} interface.
 * Provides concrete logic for generating, retrieving, building, and
 * persisting TA application forms. Uses an AI client (with a fallback)
 * for automatic form generation from resume text, and delegates
 * persistence to {@link ApplicationFormRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class ApplicationFormServiceImpl implements ApplicationFormService {
    /** Primary AI client for generating application form content. */
    private final ApplicationFormAiClient aiClient;

    /** Fallback AI client used when the primary client is unavailable. */
    private final ApplicationFormAiClient fallbackAiClient;

    /** Text extractor for reading resume file content. */
    private final ResumeTextExtractor resumeTextExtractor;

    /** Repository for application form persistence. */
    private final ApplicationFormRepository applicationFormRepository;

    /**
     * Constructs a new {@code ApplicationFormServiceImpl} with default
     * dependencies: an {@link ApplicationFormAiClientFactory} instance,
     * a {@link MockApplicationFormAiClient} fallback, a
     * {@link PdfBoxResumeTextExtractor}, and a
     * {@link TxtApplicationFormRepositoryImpl}.
     */
    public ApplicationFormServiceImpl() {
        this(new ApplicationFormAiClientFactory().create(), new MockApplicationFormAiClient(),
                new PdfBoxResumeTextExtractor(), new TxtApplicationFormRepositoryImpl());
    }

    /**
     * Constructs a new {@code ApplicationFormServiceImpl} with the given
     * dependencies.
     *
     * @param aiClient                   the primary AI client for form generation
     * @param fallbackAiClient           the fallback AI client
     * @param resumeTextExtractor        the extractor for reading resume text
     * @param applicationFormRepository  the repository for form persistence
     */
    ApplicationFormServiceImpl(ApplicationFormAiClient aiClient, ApplicationFormAiClient fallbackAiClient,
            ResumeTextExtractor resumeTextExtractor, ApplicationFormRepository applicationFormRepository) {
        this.aiClient = aiClient;
        this.fallbackAiClient = fallbackAiClient;
        this.resumeTextExtractor = resumeTextExtractor;
        this.applicationFormRepository = applicationFormRepository;
    }

    /**
     * Generates an initial application form for a TA applying to a course by
     * extracting the TA's resume text and invoking the AI generation pipeline
     * with fallback support.
     *
     * @param ta     the TA applicant
     * @param course the course being applied to
     * @return the generated {@link ApplicationForm}
     */
    @Override
    public ApplicationForm generateInitialForm(TA ta, Course course) {
        String resumeText = extractResumeText(ta);
        ApplicationForm form = generateWithFallback(ta, course, resumeText);
        applicationFormRepository.saveOrUpdate(form);
        return form;
    }

    /**
     * Retrieves an existing application form for the given TA email and course ID.
     *
     * @param taEmail  the email address of the TA
     * @param courseId the unique identifier of the course
     * @return the matching {@link ApplicationForm}, or {@code null} if not found
     */
    @Override
    public ApplicationForm getForm(String taEmail, String courseId) {
        return applicationFormRepository.findForm(taEmail, courseId);
    }

    /**
     * Builds an {@link ApplicationForm} from the given HTTP request parameters.
     *
     * @param ta                 the TA applicant
     * @param course             the course being applied to
     * @param applicantName      the applicant's display name
     * @param email              the applicant's email address
     * @param education          the education background text
     * @param skills             the skills description
     * @param relevantExperience the relevant experience description
     * @param projectExperience  the project experience description
     * @param feedback           additional feedback or notes
     * @param submitted          whether the form has been submitted
     * @return the constructed {@link ApplicationForm}
     */
    @Override
    public ApplicationForm buildFormFromRequest(TA ta, Course course,
            String applicantName, String email, String education, String skills,
            String relevantExperience, String projectExperience, String feedback, boolean submitted) {
        ApplicationForm existingForm = applicationFormRepository.findForm(ta.getEmail(), course.getId());
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
        form.setApplicantName(trimValue(applicantName));
        form.setEmail(trimValue(email));
        form.setEducation(trimValue(education));
        form.setSkills(trimValue(skills));
        form.setRelevantExperience(trimValue(relevantExperience));
        form.setProjectExperience(trimValue(projectExperience));
        form.setFeedback(trimValue(feedback));
        form.setSubmitted(submitted);
        return form;
    }

    /**
     * Persists an application form, creating a new record or updating an existing one.
     *
     * @param form the {@link ApplicationForm} to save or update
     */
    @Override
    public void saveForm(ApplicationForm form) {
        applicationFormRepository.saveOrUpdate(form);
    }

    /**
     * Generates an application form using the primary AI client, falling back
     * to the secondary client or a minimal default form if both fail.
     *
     * @param ta         the TA applicant
     * @param course     the course being applied to
     * @param resumeText the extracted resume text
     * @return a generated {@link ApplicationForm}
     */
    private ApplicationForm generateWithFallback(TA ta, Course course, String resumeText) {
        try {
            return aiClient.generate(ta, course, resumeText);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            try {
                ApplicationForm form = fallbackAiClient.generate(ta, course, resumeText);
                form.setFeedback("Groq generation is unavailable, so this mock draft was generated locally. "
                        + form.getFeedback());
                return form;
            } catch (IOException | InterruptedException ignored) {
                ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
                form.setEmail(ta.getEmail());
                form.setEducation("Not provided");
                form.setSkills("Not provided");
                form.setRelevantExperience("Not provided");
                form.setProjectExperience("Not provided");
                form.setFeedback("AI feedback is temporarily unavailable. Please add concrete evidence matching the job requirement.");
                return form;
            }
        }
    }

    /**
     * Extracts plain text from the TA's master resume file.
     *
     * @param ta the TA whose resume should be extracted
     * @return the extracted resume text, or an empty string if extraction fails
     */
    private String extractResumeText(TA ta) {
        if (ta == null || ta.getMasterResumeDirectory() == null || ta.getMasterResumeDirectory().isBlank()) {
            return "";
        }

        File resumeFile = new File(ta.getMasterResumeDirectory(), buildStoredResumeFileName(ta));
        try {
            return resumeTextExtractor.extract(resumeFile);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Trims a string value, returning an empty string for null input.
     *
     * @param value the string to trim
     * @return the trimmed string, or an empty string if the input is {@code null}
     */
    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Builds the stored resume file name for the given TA by normalizing
     * the email address and appending a {@code .pdf} extension.
     *
     * @param ta the TA whose resume file name is to be built
     * @return the normalized resume file name
     */
    private String buildStoredResumeFileName(TA ta) {
        String normalizedEmail = ta == null || ta.getEmail() == null || ta.getEmail().trim().isEmpty()
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizedEmail + ".pdf";
    }
}
