package com.harekrishna.translator.service;

import com.harekrishna.translator.model.Translation;
import com.harekrishna.translator.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final WebClient webClient;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.base-url}")
    private String baseUrl;

    public TranslationService(TranslationRepository translationRepository, WebClient.Builder webClientBuilder) {
        this.translationRepository = translationRepository;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Translates a text without saving it to the repository immediately.
     */
    public Mono<String> translateOnly(String sourceText, String sourceLang, String targetLang) {
        String systemPrompt = String.format(
                "You are an expert translator specializing in Krishna conscious spiritual books. " +
                "Translate the following %s text into %s. " +
                "The translation must reflect the author's mood and intent, maintaining the spiritual depth and devotional fervor found in classic Vaishnava literature. " +
                "Use appropriate %s spiritual terminology and ensure the tone is respectful and spiritually uplifting. " +
                "CRITICAL: 1. Use ONLY pure %s script characters; do not mix characters from other languages. " +
                "2. Provide ONLY the plain text translation. Do not use any Markdown formatting.",
                sourceLang, targetLang, targetLang, targetLang
        );

        return webClient.post()
                .uri(baseUrl + (baseUrl.endsWith("/") ? "chat/completions" : "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", sourceText)
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                });
    }

    public Translation save(Translation translation) {
        return translationRepository.save(translation);
    }
}
