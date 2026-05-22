package service.ai.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import service.ai.MOCourseDraftAiClient;

public class GroqMOCourseDraftAiClient implements MOCourseDraftAiClient {
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    public MOCourseDraft generate(String courseName) throws IOException, InterruptedException {
        String apiKey = readConfig("GROQ_API_KEY", "");
        if (apiKey.isBlank()) {
            throw new IOException("GROQ_API_KEY is not configured.");
        }

        String model = readConfig("GROQ_MODEL", "llama-3.1-8b-instant");
        String responseBody = sendWithPowerShell(apiKey, buildRequestBody(model, courseName));
        System.out.println("[Groq] mo-course-draft transport=powershell");
        String content = extractMessageContent(responseBody);
        return parseDraft(content);
    }

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
                + "]"
                + "}";
    }

    private MOCourseDraft parseDraft(String json) {
        return new MOCourseDraft(
                extractJsonValue(json, "jobTitle"),
                extractJsonValue(json, "jobDescription"),
                extractJsonValue(json, "jobRequirement"));
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
        int startQuote = colonIndex < 0 ? -1 : json.indexOf('"', colonIndex + 1);
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

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
