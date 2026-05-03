package com.harekrishna.translator.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

public class LlmBaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "llmBaseUrlSanitizer";
    private static final String PROPERTY_NAME = "LLM_BASE_URL";
    private static final String CHAT_COMPLETIONS_SUFFIX = "/chat/completions";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawBaseUrl = environment.getProperty(PROPERTY_NAME);
        if (rawBaseUrl == null || rawBaseUrl.isBlank()) {
            return;
        }

        String normalizedBaseUrl = normalize(rawBaseUrl);
        if (normalizedBaseUrl.equals(rawBaseUrl)) {
            return;
        }

        Map<String, Object> sanitizedProperties = new LinkedHashMap<>();
        sanitizedProperties.put(PROPERTY_NAME, normalizedBaseUrl);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, sanitizedProperties));
    }

    static String normalize(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith(CHAT_COMPLETIONS_SUFFIX)) {
            normalized = normalized.substring(0, normalized.length() - CHAT_COMPLETIONS_SUFFIX.length());
        }
        return normalized;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}