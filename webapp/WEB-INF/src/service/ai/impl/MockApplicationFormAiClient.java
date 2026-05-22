package service.ai.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ai.ApplicationFormAiClient;

public class MockApplicationFormAiClient implements ApplicationFormAiClient {
    @Override
    public ApplicationForm generate(TA ta, Course course, String resumeText) {
        ResumeSummary summary = summarizeResume(resumeText);
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
        form.setApplicantName(defaultValue(ta.getName(), summary.name));
        form.setEmail(defaultValue(ta.getEmail(), ""));
        form.setEducation(defaultValue(ta.getCollege(), defaultValue(summary.education, "Not provided")));
        form.setSkills(defaultValue(ta.getSkill(), defaultValue(summary.skills, "Not provided")));
        form.setRelevantExperience(defaultValue(summary.experience,
                "Not provided. Please add teaching, tutoring, grading, lab, or project experience relevant to this course."));
        form.setProjectExperience(defaultValue(summary.projects,
                "Not provided. Please add projects that show your fit for " + safe(course.getCourseName()) + "."));
        form.setFeedback("Strengthen the application by adding concrete evidence matching the job requirement: "
                + safe(course.getJobRequirement()) + ".");
        form.setSubmitted(false);
        return form;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "this course" : value.trim();
    }

    private ResumeSummary summarizeResume(String resumeText) {
        ResumeSummary summary = new ResumeSummary();
        if (resumeText == null || resumeText.isBlank()) {
            return summary;
        }

        List<String> lines = cleanLines(resumeText);
        summary.name = firstLikelyName(lines);
        summary.education = collectMatching(lines, "university", "college", "bachelor", "master", "phd", "degree", "gpa", "bupt");
        summary.skills = collectSection(lines, "skill", "technical skill", "programming", "technology");
        summary.experience = collectSection(lines, "experience", "internship", "competition", "award", "teaching", "tutor");
        summary.projects = collectSection(lines, "project", "portfolio", "system", "platform");

        if (summary.skills.isBlank()) {
            summary.skills = collectMatching(lines, "java", "python", "javascript", "spring", "sql", "linux", "git",
                    "react", "docker", "algorithm");
        }
        if (summary.projects.isBlank()) {
            summary.projects = collectMatching(lines, "built", "developed", "implemented", "designed", "optimized",
                    "deployed", "system", "platform");
        }
        return summary;
    }

    private List<String> cleanLines(String resumeText) {
        String normalized = resumeText.replace('\r', '\n');
        String[] rawLines = normalized.split("\\n+");
        List<String> lines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String line = rawLine.replaceAll("\\s+", " ").trim();
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private String firstLikelyName(List<String> lines) {
        for (String line : lines) {
            if (line.length() <= 40 && line.matches("[A-Z][A-Za-z.'-]+(\\s+[A-Z][A-Za-z.'-]+){0,3}")) {
                return line;
            }
        }
        return "";
    }

    private String collectSection(List<String> lines, String... markers) {
        for (int i = 0; i < lines.size(); i++) {
            if (!containsAny(lines.get(i), markers)) {
                continue;
            }

            List<String> values = new ArrayList<>();
            String markerLine = lines.get(i);
            if (markerLine.contains(":")) {
                String afterColon = markerLine.substring(markerLine.indexOf(':') + 1).trim();
                if (!afterColon.isBlank()) {
                    values.add(afterColon);
                }
            }

            for (int j = i + 1; j < lines.size() && values.size() < 4; j++) {
                String candidate = lines.get(j);
                if (looksLikeSectionHeader(candidate) && !containsAny(candidate, markers)) {
                    break;
                }
                values.add(candidate);
            }
            return joinLimited(values);
        }
        return "";
    }

    private String collectMatching(List<String> lines, String... keywords) {
        List<String> values = new ArrayList<>();
        for (String line : lines) {
            if (containsAny(line, keywords)) {
                values.add(line);
            }
            if (values.size() >= 4) {
                break;
            }
        }
        return joinLimited(values);
    }

    private boolean containsAny(String line, String... keywords) {
        String lower = line.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeSectionHeader(String line) {
        return line.length() <= 35 && line.equals(line.toUpperCase(Locale.ROOT));
    }

    private String joinLimited(List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        String joined = String.join("\n", values);
        return joined.length() <= 900 ? joined : joined.substring(0, 900);
    }

    private static class ResumeSummary {
        private String name = "";
        private String education = "";
        private String skills = "";
        private String experience = "";
        private String projects = "";
    }
}
