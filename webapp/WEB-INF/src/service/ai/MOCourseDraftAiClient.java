package service.ai;

import java.io.IOException;

/**
 * Client interface for AI-powered generation of MOOC / course TA recruitment
 * drafts.
 * <p>
 * Implementations use AI services to produce an initial draft containing a
 * job title, job description, and job requirement for a given course.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see service.ai.impl.GroqMOCourseDraftAiClient
 * @see service.ai.impl.MockMOCourseDraftAiClient
 */
public interface MOCourseDraftAiClient {
    /**
     * Generates a TA recruitment draft for the specified course.
     *
     * @param courseName the name of the course for which to generate a draft
     * @return a {@link MOCourseDraft} containing the AI-generated job title,
     *         description, and requirements
     * @throws IOException          if communication with the AI service fails
     * @throws InterruptedException if the request thread is interrupted
     */
    MOCourseDraft generate(String courseName) throws IOException, InterruptedException;

    /**
     * Immutable data class representing an AI-generated MOOC / course TA
     * recruitment draft.
     * <p>
     * Holds the three core fields that course owners typically edit before
     * publishing: job title, job description, and job requirement.
 *
     *
     * @author TA Recruitment Team
     * @version 1.0
     * @since 2025-03-01
     */
    final class MOCourseDraft {
        /** The proposed job title for the TA position. */
        private final String jobTitle;
        /** The detailed description of the TA duties. */
        private final String jobDescription;
        /** The qualifications and skills required for the position. */
        private final String jobRequirement;

        /**
         * Constructs a new {@code MOCourseDraft} with the given values.
         *
         * @param jobTitle        the proposed job title
         * @param jobDescription  the detailed description of TA duties
         * @param jobRequirement  the qualifications and skills required
         */
        public MOCourseDraft(String jobTitle, String jobDescription, String jobRequirement) {
            this.jobTitle = jobTitle;
            this.jobDescription = jobDescription;
            this.jobRequirement = jobRequirement;
        }

        /**
         * Returns the proposed job title.
         *
         * @return the job title string
         */
        public String getJobTitle() {
            return jobTitle;
        }

        /**
         * Returns the detailed description of the TA duties.
         *
         * @return the job description string
         */
        public String getJobDescription() {
            return jobDescription;
        }

        /**
         * Returns the qualifications and skills required for the position.
         *
         * @return the job requirement string
         */
        public String getJobRequirement() {
            return jobRequirement;
        }
    }
}
