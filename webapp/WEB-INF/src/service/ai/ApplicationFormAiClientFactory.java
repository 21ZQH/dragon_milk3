package service.ai;

import service.ai.impl.GroqApplicationFormAiClient;
import service.ai.impl.MockApplicationFormAiClient;

public class ApplicationFormAiClientFactory {
    public ApplicationFormAiClient create() {
        String provider = readConfig("APPLICATION_FORM_AI_PROVIDER", "groq");
        if ("groq".equalsIgnoreCase(provider) && !readConfig("GROQ_API_KEY", "").isBlank()) {
            return new GroqApplicationFormAiClient();
        }
        return new MockApplicationFormAiClient();
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
