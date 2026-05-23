package service.ai.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ai.ApplicationFormAiClient;

/**
 * Mock implementation of {@link ApplicationFormAiClient} that generates
 * application forms using heuristic resume parsing rather than an external
 * AI service.
 * <p>
 * This client is used when no Groq API key is configured or when the
 * provider is explicitly set to a non-Groq value. It scans the resume text
 * for common section headers and keywords to populate form fields, providing
 * reasonable defaults for local development and testing.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see ApplicationFormAiClient
 * @see GroqApplicationFormAiClient
 */
public class MockApplicationFormAiClient implements ApplicationFormAiClient {
    /**
     * Generates a mock application form by heuristically parsing the resume
     * text and combining it with the TA's existing profile data.
     *
     * @param ta         the teaching assistant applicant
     * @param course     the target course
     * @param resumeText the full resume text to analyse
     * @return a populated {@link ApplicationForm} with heuristic values
     */
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

    /**
     * Returns the primary value if it is non-null and non-blank, otherwise
     * the fallback value.
     *
     * @param value    the primary value
     * @param fallback the fallback value
     * @return {@code value} if non-null and non-blank, else {@code fallback},
     *         trimmed in either case
     */
    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /**
     * Returns a trimmed string if the input is non-null and non-blank,
     * otherwise returns a default message.
     *
     * @param value the string to check
     * @return the trimmed original string, or {@code "this course"} if blank
     */
    private String safe(String value) {
        return value == null || value.isBlank() ? "this course" : value.trim();
    }

    /**
     * Heuristically parses the resume text to extract structured summary
     * information such as name, education, skills, experience, and projects.
     *
     * @param resumeText the raw resume text
     * @return a {@link ResumeSummary} containing extracted fields
     */
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

    /**
     * Splits the resume text into cleaned, non-blank lines.
     *
     * @param resumeText the raw resume text
     * @return a list of cleaned lines
     */
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

    /**
     * Finds the first line that looks like a person's name based on
     * capitalisation patterns.
     *
     * @param lines the cleaned resume lines
     * @return the first likely name, or an empty string if none found
     */
    private String firstLikelyName(List<String> lines) {
        for (String line : lines) {
            if (line.length() <= 40 && line.matches("[A-Z][A-Za-z.'-]+(\\s+[A-Z][A-Za-z.'-]+){0,3}")) {
                return line;
            }
        }
        return "";
    }

    /**
     * Collects text from a section of the resume identified by keyword
     * markers, taking up to 4 subsequent lines as content.
     *
     * @param lines   the cleaned resume lines
     * @param markers keywords that identify the section header
     * @return the collected section text, or an empty string if no section
     *         is found
     */
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

    /**
     * Collects lines that contain any of the specified keywords.
     *
     * @param lines    the cleaned resume lines
     * @param keywords the keywords to search for (case-insensitive)
     * @return the matching lines joined together, limited to 4 entries
     */
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

    /**
     * Checks whether a line contains any of the specified keywords
     * (case-insensitive).
     *
     * @param line     the line to check
     * @param keywords the keywords to search for
     * @return {@code true} if any keyword is found; {@code false} otherwise
     */
    private boolean containsAny(String line, String... keywords) {
        String lower = line.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a line looks like a section header (all-uppercase
     * and short).
     *
     * @param line the line to check
     * @return {@code true} if the line is all uppercase and 35 characters
     *         or fewer; {@code false} otherwise
     */
    private boolean looksLikeSectionHeader(String line) {
        return line.length() <= 35 && line.equals(line.toUpperCase(Locale.ROOT));
    }

    /**
     * Joins a list of values with newlines, limiting the total length to
     * 900 characters.
     *
     * @param values the list of strings to join
     * @return the joined string, or an empty string if the list is empty
     */
    private String joinLimited(List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        String joined = String.join("\n", values);
        return joined.length() <= 900 ? joined : joined.substring(0, 900);
    }

    /**
     * Internal data holder for the heuristically extracted resume summary.
     */
    private static class ResumeSummary {
        /** The applicant's likely name. */
        private String name = "";
        /** Extracted education entries. */
        private String education = "";
        /** Extracted skills. */
        private String skills = "";
        /** Extracted relevant experience. */
        private String experience = "";
        /** Extracted project experience. */
        private String projects = "";
    }
}
