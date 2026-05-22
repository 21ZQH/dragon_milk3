package service.ai.impl;

import java.io.IOException;

import service.ai.MOCourseDraftAiClient;

public class MockMOCourseDraftAiClient implements MOCourseDraftAiClient {
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
