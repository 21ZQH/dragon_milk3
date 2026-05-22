package service.ai;

import service.ai.impl.GroqMOCourseDraftAiClient;
import service.ai.impl.MockMOCourseDraftAiClient;

/**
 * Factory for creating {@link MOCourseDraftAiClient} instances.
 * <p>
 * Reads the {@code MO_COURSE_DRAFT_AI_PROVIDER} configuration (from system
 * property, environment variable, or fallback) to decide whether to return a
 * real Groq-backed client or a mock client for local development / testing.
 * </p>
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see GroqMOCourseDraftAiClient
 * @see MockMOCourseDraftAiClient
 */
public class MOCourseDraftAiClientFactory {
    /**
     * Creates and returns an appropriate {@link MOCourseDraftAiClient}
     * based on the configured provider.
     *
     * @return a Groq-backed client if the provider is set to {@code "groq"}
     *         and a valid API key is present; otherwise a mock client
     */
    public MOCourseDraftAiClient create() {
        String provider = readConfig("MO_COURSE_DRAFT_AI_PROVIDER", "groq");
        if ("groq".equalsIgnoreCase(provider) && !readConfig("GROQ_API_KEY", "").isBlank()) {
            return new GroqMOCourseDraftAiClient();
        }
        return new MockMOCourseDraftAiClient();
    }

    /**
     * Reads a configuration value from the following sources in order:
     * <ol>
     *   <li>System property ({@code System.getProperty(key)})</li>
     *   <li>Environment variable ({@code System.getenv(key)})</li>
     *   <li>Fallback default value</li>
     * </ol>
     *
     * @param key      the configuration key to look up
     * @param fallback the default value to return if the key is not found
     * @return the resolved configuration value, or the fallback if absent
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
}
