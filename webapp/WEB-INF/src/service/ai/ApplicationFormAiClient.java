package service.ai;

import java.io.IOException;

import model.ApplicationForm;
import model.Course;
import model.TA;

/**
 * Client interface for AI-powered generation of TA application forms.
 * <p>
 * Implementations of this interface interact with AI services (e.g., Groq API)
 * to produce tailored {@link ApplicationForm} instances based on a
 * {@link TA}'s profile, a target {@link Course}, and the applicant's
 * resume text.
 * </p>
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see GroqApplicationFormAiClient
 * @see MockApplicationFormAiClient
 */
public interface ApplicationFormAiClient {
    /**
     * Generates an application form by having an AI model analyse the given
     * resume text together with the course and TA information.
     *
     * @param ta         the teaching assistant applicant whose information is
     *                   used to seed the form
     * @param course     the course for which the application is being submitted
     * @param resumeText the full text extracted from the applicant's resume
     * @return a populated {@link ApplicationForm} with AI-generated fields
     * @throws IOException          if communication with the AI service fails
     * @throws InterruptedException if the request thread is interrupted
     */
    ApplicationForm generate(TA ta, Course course, String resumeText) throws IOException, InterruptedException;
}
