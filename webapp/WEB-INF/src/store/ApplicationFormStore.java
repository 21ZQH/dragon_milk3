package store;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import model.ApplicationForm;

/**
 * Data access layer for persisting and retrieving TA application forms.
 *
 * <p>Application forms are stored in a tab-separated file with fields encoded
 * using Base64 URL-safe encoding for safe storage. Each form is uniquely
 * identified by the combination of TA email and course ID.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class ApplicationFormStore {
    /** System property key for overriding the default application form store file path. */
    public static final String FILE_PATH_PROPERTY = "application.form.store.path";

    /**
     * Finds an application form by TA email and course ID.
     *
     * @param taEmail  the email address of the TA
     * @param courseId the unique identifier of the course
     * @return the matching {@link ApplicationForm}, or {@code null} if not found
     */
    public static ApplicationForm findForm(String taEmail, String courseId) {
        if (taEmail == null || courseId == null) {
            return null;
        }

        for (ApplicationForm form : getAllForms()) {
            if (taEmail.equals(form.getTaEmail()) && courseId.equals(form.getCourseId())) {
                return form;
            }
        }
        return null;
    }

    /**
     * Saves a new application form or updates an existing one.
     * <p>If a form with the same TA email and course ID already exists, it is
     * replaced; otherwise the form is appended to the store.</p>
     *
     * @param form the application form to save or update
     */
    public static void saveOrUpdate(ApplicationForm form) {
        if (form == null || form.getTaEmail() == null || form.getCourseId() == null) {
            return;
        }

        List<ApplicationForm> forms = getAllForms();
        boolean updated = false;
        for (int i = 0; i < forms.size(); i++) {
            ApplicationForm existing = forms.get(i);
            if (form.getTaEmail().equals(existing.getTaEmail())
                    && form.getCourseId().equals(existing.getCourseId())) {
                forms.set(i, form);
                updated = true;
                break;
            }
        }
        if (!updated) {
            forms.add(form);
        }

        List<String> lines = new ArrayList<>();
        for (ApplicationForm savedForm : forms) {
            lines.add(toLine(savedForm));
        }

        Path filePath = resolveFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all application form records from the store.
     */
    public static void clearAll() {
        Path filePath = resolveFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, List.of(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all application forms from the store.
     *
     * @return a list of all stored application forms
     */
    private static List<ApplicationForm> getAllForms() {
        List<ApplicationForm> forms = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return forms;
        }

        try {
            for (String line : Files.readAllLines(filePath)) {
                ApplicationForm form = parseLine(line);
                if (form != null) {
                    forms.add(form);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return forms;
    }

    /**
     * Parses a single tab-separated line into an {@link ApplicationForm} object.
     * <p>Fields are decoded from Base64 URL-safe encoding. Supports both
     * 11-field and 12-field line formats for backward compatibility.</p>
     *
     * @param line the tab-separated line to parse
     * @return the parsed application form, or {@code null} if the line is invalid
     */
    private static ApplicationForm parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split("\\t", -1);
        if (parts.length < 11) {
            return null;
        }

        ApplicationForm form = new ApplicationForm(decode(parts[0]), decode(parts[1]));
        form.setApplicantName(decode(parts[2]));
        form.setEmail(decode(parts[3]));
        form.setEducation(decode(parts[4]));
        form.setSkills(decode(parts[5]));
        form.setRelevantExperience(decode(parts[6]));
        form.setProjectExperience(decode(parts[7]));
        int feedbackIndex = parts.length >= 12 ? 9 : 8;
        int submittedIndex = parts.length >= 12 ? 10 : 9;
        int updatedAtIndex = parts.length >= 12 ? 11 : 10;
        form.setFeedback(decode(parts[feedbackIndex]));
        form.setSubmitted(Boolean.parseBoolean(parts[submittedIndex]));
        try {
            form.setUpdatedAt(LocalDateTime.parse(parts[updatedAtIndex]));
        } catch (Exception ignored) {
            form.setUpdatedAt(LocalDateTime.now());
        }
        return form;
    }

    /**
     * Converts an {@link ApplicationForm} to a tab-separated line.
     * <p>All text fields are encoded using Base64 URL-safe encoding. The
     * updated-at timestamp is refreshed to the current time.</p>
     *
     * @param form the application form to serialize
     * @return the tab-separated line string
     */
    private static String toLine(ApplicationForm form) {
        form.setUpdatedAt(LocalDateTime.now());
        return String.join("\t",
                encode(form.getTaEmail()),
                encode(form.getCourseId()),
                encode(form.getApplicantName()),
                encode(form.getEmail()),
                encode(form.getEducation()),
                encode(form.getSkills()),
                encode(form.getRelevantExperience()),
                encode(form.getProjectExperience()),
                encode(form.getFeedback()),
                String.valueOf(form.isSubmitted()),
                form.getUpdatedAt().toString());
    }

    /**
     * Encodes a string value using Base64 URL-safe encoding without padding.
     *
     * @param value the string to encode; {@code null} values are treated as empty strings
     * @return the Base64-encoded string
     */
    private static String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64 URL-safe encoded string back to its original value.
     *
     * @param value the Base64-encoded string to decode
     * @return the decoded string, or an empty string if the input is blank
     */
    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    /**
     * Resolves the application form store file path from system properties or defaults.
     *
     * @return the resolved file path
     */
    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "application-forms.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "application-forms.txt");
    }

    /**
     * Ensures the parent directory of the given file path exists, creating it if necessary.
     *
     * @param filePath the file path whose parent directory should be checked
     * @throws IOException if an I/O error occurs during directory creation
     */
    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}
