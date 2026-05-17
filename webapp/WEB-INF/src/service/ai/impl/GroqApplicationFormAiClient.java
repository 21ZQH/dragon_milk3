package service.ai.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ai.ApplicationFormAiClient;

public class GroqApplicationFormAiClient implements ApplicationFormAiClient {
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient httpClient;

    public GroqApplicationFormAiClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build());
    }

    GroqApplicationFormAiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ApplicationForm generate(TA ta, Course course, String resumeText) throws IOException, InterruptedException {
        String apiKey = readConfig("GROQ_API_KEY", "");
        if (apiKey.isBlank()) {
            throw new IOException("GROQ_API_KEY is not configured.");
        }

        String model = readConfig("GROQ_MODEL", "llama-3.1-8b-instant");
        String body = buildRequestBody(model, ta, course, resumeText);
        String responseBody = sendRequest(apiKey, body);
        String content = extractMessageContent(responseBody);
        return parseApplicationForm(ta, course, content);
    }

    private String sendRequest(String apiKey, String body) throws IOException, InterruptedException {
        try {
            String responseBody = sendWithJavaHttpClient(apiKey, body);
            System.out.println("[Groq] transport=java-http");
            logTransport("java-http");
            return responseBody;
        } catch (IOException javaHttpError) {
            if (!isWindows()) {
                throw javaHttpError;
            }

            try {
                String responseBody = sendWithPowerShell(apiKey, body);
                System.out.println("[Groq] transport=powershell fallback; javaHttpError=" + javaHttpError.getMessage());
                logTransport("powershell fallback; javaHttpError=" + javaHttpError.getMessage());
                return responseBody;
            } catch (IOException powerShellError) {
                powerShellError.addSuppressed(javaHttpError);
                throw powerShellError;
            }
        }
    }

    private String sendWithJavaHttpClient(String apiKey, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Groq API request failed through Java HTTP: HTTP "
                    + response.statusCode() + " " + limitText(response.body(), 500));
        }

        return response.body();
    }

    private String sendWithPowerShell(String apiKey, String body) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                "$body = [Console]::In.ReadToEnd(); "
                        + "$headers = @{ 'Content-Type'='application/json'; 'Authorization'='Bearer ' + $env:GROQ_API_KEY }; "
                        + "$response = Invoke-RestMethod -Uri '" + ENDPOINT + "' -Method Post -Headers $headers -Body $body -TimeoutSec 60; "
                        + "$response | ConvertTo-Json -Depth 20 -Compress");
        processBuilder.environment().put("GROQ_API_KEY", apiKey);

        Process process = processBuilder.start();
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(body.getBytes(StandardCharsets.UTF_8));
        }

        boolean finished = process.waitFor(75, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Groq API request through PowerShell timed out.");
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.exitValue() != 0) {
            throw new IOException("Groq API request through PowerShell failed: " + limitText(stderr, 500));
        }
        if (stdout.isBlank()) {
            throw new IOException("Groq API request through PowerShell returned an empty response.");
        }
        return stdout;
    }

    private String buildRequestBody(String model, TA ta, Course course, String resumeText) {
        String systemPrompt = """
                You generate job-specific TA application forms.
                Return strict JSON only, with no markdown or explanation.
                Required JSON keys:
                applicantName, email, education, skills, relevantExperience, projectExperience, courseFit, feedback.
                Every JSON value must be a plain string. Do not return arrays, nested objects, or null values.
                Do not invent unsupported facts. Use "Not provided" when resume information is missing.
                Tailor courseFit and feedback to the target job.
                The feedback field must be advice to the applicant, written in second person.
                Do not write feedback as an applicant statement. Avoid "I", "my", "me", or "I believe".
                Feedback should name specific missing evidence or improvements that would make the application stronger.
                """;
        String userPrompt = """
                Resume text:
                %s

                Job information:
                Course: %s
                Job title: %s
                Working hours: %s
                Description: %s
                Requirement: %s
                Applicant account email: %s
                """.formatted(
                limitText(resumeText, 16000),
                safe(course.getCourseName()),
                safe(course.getJobTitle()),
                safe(course.getWorkingHours()),
                safe(course.getJobDescription()),
                safe(course.getJobRequirement()),
                safe(ta.getEmail()));

        return "{"
                + "\"model\":\"" + escapeJson(model) + "\","
                + "\"temperature\":0.2,"
                + "\"max_completion_tokens\":1200,"
                + "\"response_format\":{\"type\":\"json_object\"},"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userPrompt) + "\"}"
                + "]"
                + "}";
    }

    private ApplicationForm parseApplicationForm(TA ta, Course course, String json) {
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
        form.setApplicantName(extractJsonValue(json, "applicantName"));
        form.setEmail(defaultValue(extractJsonValue(json, "email"), ta.getEmail()));
        form.setEducation(extractJsonValue(json, "education"));
        form.setSkills(extractJsonValue(json, "skills"));
        form.setRelevantExperience(extractJsonValue(json, "relevantExperience"));
        form.setProjectExperience(extractJsonValue(json, "projectExperience"));
        form.setCourseFit(extractJsonValue(json, "courseFit"));
        form.setFeedback(extractJsonValue(json, "feedback"));
        form.setSubmitted(false);
        return form;
    }

    private String extractMessageContent(String responseBody) throws IOException {
        String marker = "\"content\"";
        int markerIndex = responseBody.indexOf(marker);
        if (markerIndex < 0) {
            throw new IOException("Groq API response did not include message content.");
        }

        int colonIndex = responseBody.indexOf(':', markerIndex + marker.length());
        int quoteIndex = responseBody.indexOf('"', colonIndex + 1);
        if (colonIndex < 0 || quoteIndex < 0) {
            throw new IOException("Groq API response content is malformed.");
        }

        return readJsonString(responseBody, quoteIndex);
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\"";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0) {
            return "";
        }

        int colonIndex = json.indexOf(':', markerIndex + marker.length());
        if (colonIndex < 0) {
            return "";
        }

        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return "";
        }
        return readJsonString(json, startQuote);
    }

    private String readJsonString(String json, int startQuote) {
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                switch (ch) {
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case '"' -> value.append('"');
                    case '\\' -> value.append('\\');
                    default -> value.append(ch);
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return value.toString();
            } else {
                value.append(ch);
            }
        }
        return value.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String limitText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String readConfig(String key, String fallback) {
        String propertyValue = System.getProperty(key);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String windowsUserValue = readWindowsUserEnvironment(key);
        if (windowsUserValue != null && !windowsUserValue.isBlank()) {
            return windowsUserValue;
        }
        return fallback;
    }

    private String readWindowsUserEnvironment(String key) {
        if (!isWindows() || key == null || key.isBlank()) {
            return "";
        }

        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", key).start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "";
            }
            if (process.exitValue() != 0) {
                return "";
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String line : output.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith(key + " ")) {
                    String[] parts = trimmed.split("\\s+", 3);
                    return parts.length >= 3 ? parts[2].trim() : "";
                }
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return "";
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private void logTransport(String message) {
        try {
            Path logPath = resolveRuntimeFilePath("groq-transport.log");
            Files.createDirectories(logPath.getParent());
            Files.writeString(
                    logPath,
                    LocalDateTime.now() + " " + message + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private Path resolveRuntimeFilePath(String fileName) {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", fileName);
        }
        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", fileName);
    }
}
