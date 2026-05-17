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

public class ApplicationFormStore {
    public static final String FILE_PATH_PROPERTY = "application.form.store.path";

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

    private static ApplicationForm parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split("\\t", -1);
        if (parts.length < 12) {
            return null;
        }

        ApplicationForm form = new ApplicationForm(decode(parts[0]), decode(parts[1]));
        form.setApplicantName(decode(parts[2]));
        form.setEmail(decode(parts[3]));
        form.setEducation(decode(parts[4]));
        form.setSkills(decode(parts[5]));
        form.setRelevantExperience(decode(parts[6]));
        form.setProjectExperience(decode(parts[7]));
        form.setCourseFit(decode(parts[8]));
        form.setFeedback(decode(parts[9]));
        form.setSubmitted(Boolean.parseBoolean(parts[10]));
        try {
            form.setUpdatedAt(LocalDateTime.parse(parts[11]));
        } catch (Exception ignored) {
            form.setUpdatedAt(LocalDateTime.now());
        }
        return form;
    }

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
                encode(form.getCourseFit()),
                encode(form.getFeedback()),
                String.valueOf(form.isSubmitted()),
                form.getUpdatedAt().toString());
    }

    private static String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

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

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}
