package service.impl;

import java.io.File;
import java.io.IOException;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ApplicationFormService;
import service.ai.ApplicationFormAiClient;
import service.ai.ApplicationFormAiClientFactory;
import service.ai.ResumeTextExtractor;
import service.ai.impl.MockApplicationFormAiClient;
import service.ai.impl.PdfBoxResumeTextExtractor;
import store.ApplicationFormStore;

public class ApplicationFormServiceImpl implements ApplicationFormService {
    private final ApplicationFormAiClient aiClient;
    private final ApplicationFormAiClient fallbackAiClient;
    private final ResumeTextExtractor resumeTextExtractor;

    public ApplicationFormServiceImpl() {
        this(new ApplicationFormAiClientFactory().create(), new MockApplicationFormAiClient(),
                new PdfBoxResumeTextExtractor());
    }

    ApplicationFormServiceImpl(ApplicationFormAiClient aiClient, ApplicationFormAiClient fallbackAiClient,
            ResumeTextExtractor resumeTextExtractor) {
        this.aiClient = aiClient;
        this.fallbackAiClient = fallbackAiClient;
        this.resumeTextExtractor = resumeTextExtractor;
    }

    @Override
    public ApplicationForm generateInitialForm(TA ta, Course course) {
        String resumeText = extractResumeText(ta);
        ApplicationForm form = generateWithFallback(ta, course, resumeText);
        ApplicationFormStore.saveOrUpdate(form);
        return form;
    }

    @Override
    public ApplicationForm getForm(String taEmail, String courseId) {
        return ApplicationFormStore.findForm(taEmail, courseId);
    }

    @Override
    public ApplicationForm buildFormFromRequest(TA ta, Course course,
            String applicantName, String email, String education, String skills,
            String relevantExperience, String projectExperience, String feedback, boolean submitted) {
        ApplicationForm existingForm = ApplicationFormStore.findForm(ta.getEmail(), course.getId());
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
        form.setApplicantName(trimValue(applicantName));
        form.setEmail(trimValue(email));
        form.setEducation(trimValue(education));
        form.setSkills(trimValue(skills));
        form.setRelevantExperience(trimValue(relevantExperience));
        form.setProjectExperience(trimValue(projectExperience));
        form.setCourseFit(existingForm == null ? "" : trimValue(existingForm.getCourseFit()));
        form.setFeedback(trimValue(feedback));
        form.setSubmitted(submitted);
        return form;
    }

    @Override
    public void saveForm(ApplicationForm form) {
        ApplicationFormStore.saveOrUpdate(form);
    }

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
                form.setCourseFit("Please complete this field based on the target course.");
                form.setFeedback("AI feedback is temporarily unavailable. Please add concrete evidence matching the job requirement.");
                return form;
            }
        }
    }

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

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildStoredResumeFileName(TA ta) {
        String normalizedEmail = ta == null || ta.getEmail() == null || ta.getEmail().trim().isEmpty()
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizedEmail + ".pdf";
    }
}
