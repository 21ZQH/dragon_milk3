package service.ai.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import service.ai.MOCourseDraftAiClient;

/**
 * Implementation of {@link MOCourseDraftAiClient} that communicates with
 * the Groq API to generate TA recruitment drafts for courses.
 * <p>
 * This client constructs a structured prompt from the course name, sends it
 * to Groq's chat completions endpoint via a PowerShell child process, and
 * parses the returned JSON into a {@link MOCourseDraft}.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see MOCourseDraftAiClient
 * @see MockMOCourseDraftAiClient
 */
public class GroqMOCourseDraftAiClient implements MOCourseDraftAiClient {
    /** The Groq API chat completions endpoint URL. */
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    /**
     * Generates a TA recruitment draft for the specified course by calling
     * the Groq API.
     *
     * @param courseName the name of the course
     * @return an {@link MOCourseDraft} containing the AI-generated job title,
     *         description, and requirements
     * @throws IOException          if the API key is missing, the PowerShell
     *                              transport fails, or response parsing fails
     * @throws InterruptedException if the request thread is interrupted
     */
    @Override
    public MOCourseDraft generate(String courseName) throws IOException, InterruptedException {
        String apiKey = readConfig("GROQ_API_KEY", "");
        if (apiKey.isBlank()) {
            throw new IOException("GROQ_API_KEY is not configured.");
        }

        String model = readConfig("GROQ_MODEL", DEFAULT_MODEL);
        String responseBody = sendWithPowerShell(apiKey, buildRequestBody(model, courseName));
        System.out.println("[Groq] mo-course-draft transport=powershell");
        String content = extractMessageContent(responseBody);
        return parseDraft(content);
    }

    /**
     * Sends the HTTP request to the Groq API via a PowerShell child process
     * using {@code Invoke-RestMethod}.
     *
     * @param apiKey the Groq API key
     * @param body   the JSON request body
     * @return the JSON response from the API
     * @throws IOException          if the process fails or times out
     * @throws InterruptedException if the process is interrupted
     */
    private String sendWithPowerShell(String apiKey, String body) throws IOException, InterruptedException {
        if (!isWindows()) {
            throw new IOException("PowerShell Groq transport is only available on Windows.");
        }

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
     * @param courseName the name of the course for the draft
     * @return a JSON string suitable for the Groq API
     */
    private String buildRequestBody(String model, String courseName) {
        String systemPrompt = """
                You generate initial TA recruitment drafts for university courses.
                Return strict JSON only, with no markdown or explanation.
                Required JSON keys: jobTitle, jobDescription, jobRequirement.
                Every JSON value must be a plain string. Do not return arrays, nested objects, or null values.
                Do not invent professor names, rooms, exact dates, email addresses, or unavailable facts.
                Make the draft realistic and editable by the course owner.
                In jobRequirement, include course-specific technical expectations where appropriate, such as
                professional software, lab tools, frameworks, programming languages, engineering platforms, or
                mathematical methods relevant to the course.
                Avoid generic requirements like only "communication skills"; include concrete technical evidence.
                Use normal English punctuation. Write complete sentences, not keyword fragments.
                Do not use unicode escape sequences such as \\u0027 when ordinary punctuation can be used.
                """;
        String userPrompt = """
                Course name: %s

                Generate a concise TA recruitment draft for this course.
                """.formatted(safe(courseName));

        return "{"
                + "\"model\":\"" + escapeJson(model) + "\","
                + "\"temperature\":0.3,"
                + "\"max_completion_tokens\":900,"
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
     * Parses the AI-generated JSON into an {@link MOCourseDraft}.
     *
     * @param json the JSON string containing the draft fields
     * @return a populated {@link MOCourseDraft}
     */
    private MOCourseDraft parseDraft(String json) {
        return new MOCourseDraft(
                extractJsonValue(json, "jobTitle"),
                extractJsonValue(json, "jobDescription"),
                extractJsonValue(json, "jobRequirement"));
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
        int startQuote = colonIndex < 0 ? -1 : json.indexOf('"', colonIndex + 1);
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
     * Reads a configuration value from system property or environment
     * variable, in that order.
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
        return fallback;
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
}
