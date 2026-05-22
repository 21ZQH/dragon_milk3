package service.ai;

import service.ai.impl.GroqMOCourseDraftAiClient;
import service.ai.impl.MockMOCourseDraftAiClient;

public class MOCourseDraftAiClientFactory {
    public MOCourseDraftAiClient create() {
        String provider = readConfig("MO_COURSE_DRAFT_AI_PROVIDER", "groq");
        if ("groq".equalsIgnoreCase(provider) && !readConfig("GROQ_API_KEY", "").isBlank()) {
            return new GroqMOCourseDraftAiClient();
        }
        return new MockMOCourseDraftAiClient();
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
}
