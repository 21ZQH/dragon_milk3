package service.ai.impl;

import java.io.IOException;

import service.ai.MOCourseDraftAiClient;

/**
 * Mock implementation of {@link MOCourseDraftAiClient} that generates a
 * standardised TA recruitment draft without calling an external AI service.
 * <p>
 * This client is used when no Groq API key is configured or when the
 * provider is explicitly set to a non-Groq value. It produces a simple
 * template-like draft with the course name interpolated into the description
 * and requirement fields, suitable for local development and testing.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see MOCourseDraftAiClient
 * @see GroqMOCourseDraftAiClient
 */
public class MockMOCourseDraftAiClient implements MOCourseDraftAiClient {
    /**
     * Generates a mock TA recruitment draft for the specified course.
     * <p>
     * The draft contains placeholder text that includes the course name in
     * the job description and requirements fields.
 *
     *
     * @param courseName the name of the course
     * @return a mock {@link MOCourseDraft} with a standard title and
     *         course-specific description and requirements
     * @throws IOException          never thrown by this implementation
     * @throws InterruptedException never thrown by this implementation
     */
    @Override
    public MOCourseDraft generate(String courseName) throws IOException, InterruptedException {
        String resolvedCourseName = courseName == null || courseName.isBlank() ? "this course" : courseName.trim();
        return new MOCourseDraft(
                "Teaching Assistant",
                "Support tutorials, lab sessions, assignment preparation, grading, and student questions for "
                        + resolvedCourseName + ". Coordinate with the course team to help students understand key concepts and complete practical work.",
                "Strong understanding of " + resolvedCourseName
                        + ", relevant programming languages or professional software used in the course, clear communication skills, responsibility, and the ability to guide students through technical problems.");
    }
}
