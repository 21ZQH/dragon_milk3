package service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ai.impl.MockApplicationFormAiClient;

/**
 * Unit tests for {@link MockApplicationFormAiClient} in the TA Recruitment
 * system. Verifies AI-driven application form generation from resume text.
 */
class MockApplicationFormAiClientTest {

    /**
     * Tests that the AI client generates form fields using the provided
     * resume text when the TA's profile fields are empty, extracting the
     * applicant name, education, skills, project experience, and relevant
     * experience from the resume content.
     */
    @Test
    void generateUsesResumeTextWhenProfileFieldsAreEmpty() throws Exception {
        TA ta = new TA("", "2023213248@bupt.edu.cn");
        Course course = new Course("course-1", "Software Engineering", "TA", "Java and teamwork", "Open", "MO");
        String resumeText = String.join("\n",
                "Alex Chen",
                "Education",
                "Beijing University of Posts and Telecommunications, BSc Software Engineering, GPA 3.8",
                "Technical Skills: Java, Spring Boot, SQL, React, Docker, Git",
                "Project Experience",
                "Built a TA recruitment platform with servlet controllers and layered services.",
                "Competition Experience",
                "Won a provincial software design contest using Java and teamwork.");

        ApplicationForm form = new MockApplicationFormAiClient().generate(ta, course, resumeText);

        assertEquals("Alex Chen", form.getApplicantName());
        assertTrue(form.getEducation().contains("BSc Software Engineering"));
        assertTrue(form.getSkills().contains("Spring Boot"));
        assertTrue(form.getProjectExperience().contains("TA recruitment platform"));
        assertTrue(form.getRelevantExperience().contains("software design contest"));
    }
}
