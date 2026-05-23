package service.ai.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import model.ApplicationForm;
import model.Course;
import model.TA;
import service.ai.ApplicationFormAiClient;

/**
 * Implementation of {@link ApplicationFormAiClient} that communicates with
 * the Groq API to generate TA application forms.
 * <p>
 * This client constructs a structured prompt from the TA's profile, course
 * information, and resume text, sends it to Groq's chat completions endpoint,
 * and parses the returned JSON into an {@link ApplicationForm}. The
 * transport layer uses PowerShell's {@code Invoke-RestMethod} on Windows.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see ApplicationFormAiClient
 * @see MockApplicationFormAiClient
 */
public class GroqApplicationFormAiClient implements ApplicationFormAiClient {
    /** The Groq API chat completions endpoint URL. */
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    /**
     * Constructs a new {@code GroqApplicationFormAiClient}.
     */
    public GroqApplicationFormAiClient() {
    }

    /**
     * Generates an application form by sending the TA, course, and resume
     * data to the Groq API and parsing the AI response.
     *
     * @param ta         the teaching assistant applicant
     * @param course     the target course
     * @param resumeText the full resume text
     * @return a populated {@link ApplicationForm} with AI-generated content
     * @throws IOException          if the API key is missing, the PowerShell
     *                              transport fails, or response parsing fails
     * @throws InterruptedException if the request thread is interrupted
     */
    @Override
    public ApplicationForm generate(TA ta, Course course, String resumeText) throws IOException, InterruptedException {
        String apiKey = readConfig("GROQ_API_KEY", "");
        if (apiKey.isBlank()) {
            throw new IOException("GROQ_API_KEY is not configured.");
        }

        String model = readConfig("GROQ_MODEL", DEFAULT_MODEL);
        String body = buildRequestBody(model, ta, course, resumeText);
        String responseBody = sendRequest(apiKey, body);
        String content = extractMessageContent(responseBody);
        return parseApplicationForm(ta, course, content);
    }

    /**
     * Sends the request to the Groq API via PowerShell.
     *
     * @param apiKey the Groq API key
     * @param body   the JSON request body
     * @return the raw JSON response body
     * @throws IOException          if the transport fails or the API returns
     *                              an error
     * @throws InterruptedException if the process is interrupted
     */
    private String sendRequest(String apiKey, String body) throws IOException, InterruptedException {
        if (!isWindows()) {
            throw new IOException("PowerShell Groq transport is only available on Windows.");
        }
        String responseBody = sendWithPowerShell(apiKey, body);
        System.out.println("[Groq] transport=powershell");
        logTransport("powershell");
        return responseBody;
    }

    /**
     * Executes the HTTP request to the Groq API using a PowerShell child
     * process with {@code Invoke-RestMethod}.
     *
     * @param apiKey the Groq API key passed as an environment variable
     * @param body   the JSON request body piped to the process stdin
     * @return the JSON response from the API
     * @throws IOException          if the process fails or returns a non-zero
     *                              exit code
     * @throws InterruptedException if the process is interrupted
     */
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

    /**
     * Builds the JSON request body for the Groq chat completions API.
     *
     * @param model      the model identifier (e.g., {@code llama-3.3-70b-versatile})
     * @param ta         the TA applicant
     * @param course     the target course
     * @param resumeText the applicant's resume text (truncated to 16,000 chars)
     * @return a JSON string suitable for the Groq API
     */
    private String buildRequestBody(String model, TA ta, Course course, String resumeText) {
        String systemPrompt = """
                You generate job-specific TA application forms.
                Return strict JSON only, with no markdown or explanation.
                Required JSON keys:
                applicantName, email, education, skills, relevantExperience, projectExperience, feedback.
                Every JSON value must be a plain string. Do not return arrays, nested objects, or null values.
                Do not invent unsupported facts. Use "Not provided" when resume information is missing.
                Use normal English punctuation. Write complete sentences, not keyword fragments.
                Do not use unicode escape sequences such as \\u0027 when ordinary punctuation can be used.
                Tailor feedback to the target job.
                The feedback field must be advice to the applicant, written in second person.
                Do not write feedback as an applicant statement. Avoid "I", "my", "me", or "I believe".
                Feedback should briefly separate strengths supported by the resume from missing evidence.
                Only mention a missing skill if it is required by the job but not clearly supported by the resume.
                Do not simply repeat the job requirement.
                Give two or three concrete suggestions that would make the application stronger.
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
                + "\"top_p\":1,"
                + "\"stream\":false,"
                + "\"stop\":null,"
                + "\"response_format\":{\"type\":\"json_object\"},"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userPrompt) + "\"}"
                + "]}";
    }

    /**
     * Parses the AI-generated JSON content into an {@link ApplicationForm}.
     *
     * @param ta     the TA applicant used for default values
     * @param course the target course
     * @param json   the JSON string containing application form fields
     * @return a populated {@link ApplicationForm}
     */
    private ApplicationForm parseApplicationForm(TA ta, Course course, String json) {
        ApplicationForm form = new ApplicationForm(ta.getEmail(), course.getId());
        form.setApplicantName(extractJsonValue(json, "applicantName"));
        form.setEmail(defaultValue(extractJsonValue(json, "email"), ta.getEmail()));
        form.setEducation(extractJsonValue(json, "education"));
        form.setSkills(extractJsonValue(json, "skills"));
        form.setRelevantExperience(extractJsonValue(json, "relevantExperience"));
        form.setProjectExperience(extractJsonValue(json, "projectExperience"));
        form.setFeedback(extractJsonValue(json, "feedback"));
        form.setSubmitted(false);
        return form;
    }

    /**
     * Extracts the message content from a Groq API response JSON.
     *
     * @param responseBody the raw API response JSON
     * @return the content string from the first choice message
     * @throws IOException if the response does not contain message content
     */
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

    /**
     * Extracts the string value associated with a given JSON key.
     *
     * @param json the JSON string to search
     * @param key  the key whose value should be extracted
     * @return the extracted string value, or an empty string if the key is
     *         not found
     */
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

    /**
     * Reads a JSON string literal starting from the opening quote,
     * handling escape sequences such as {@code \\n}, {@code \\t},
     * {@code \\uXXXX}, and {@code \\"}.
     *
     * @param json       the JSON string
     * @param startQuote the index of the opening double-quote character
     * @return the unescaped string content
     */
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
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                value.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                value.append('u');
                            }
                        } else {
                            value.append('u');
                        }
                    }
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

    /**
     * Escapes a string for safe inclusion in a JSON value.
     *
     * @param value the raw string to escape
     * @return the JSON-escaped string, or an empty string if the input was
     *         {@code null}
     */
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

    /**
     * Truncates a string to the specified maximum length.
     *
     * @param value     the string to truncate
     * @param maxLength the maximum number of characters to keep
     * @return the truncated string, or an empty string if the input was
     *         {@code null}
     */
    private String limitText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * Returns an empty string if the input is {@code null}, otherwise the
     * input unchanged.
     *
     * @param value the string to check
     * @return the original string, or an empty string if {@code null}
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the primary value if it is non-null and non-blank, otherwise
     * the fallback value.
     *
     * @param value    the primary value
     * @param fallback the fallback value
     * @return {@code value} if non-null and non-blank, else {@code fallback}
     */
    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Reads a configuration value from system property, environment
     * variable, or the Windows user-level registry, in that order.
     *
     * @param key      the configuration key
     * @param fallback the default value if not found
     * @return the resolved value, or {@code fallback} if absent
     */
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

    /**
     * Reads a Windows user-level environment variable from the registry
     * ({@code HKCU\Environment}).
     *
     * @param key the environment variable name
     * @return the variable value, or an empty string if not found or on
     *         non-Windows platforms
     */
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

    /**
     * Checks whether the application is running on a Windows operating
     * system.
     *
     * @return {@code true} if the {@code os.name} system property contains
     *         {@code "win"}; {@code false} otherwise
     */
    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * Appends a timestamped transport log message to the
     * {@code groq-transport.log} file.
     *
     * @param message the log message to record
     */
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

    /**
     * Resolves the runtime file path relative to the Tomcat catalina base
     * directory, falling back to the current working directory.
     *
     * @param fileName the name of the runtime file
     * @return the resolved {@link Path}
     */
    private Path resolveRuntimeFilePath(String fileName) {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", fileName);
        }
        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", fileName);
    }
}
