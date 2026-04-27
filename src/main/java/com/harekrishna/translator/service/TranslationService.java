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

    public Mono<Translation> translate(String sourceText) {
        String systemPrompt = "You are an expert translator specializing in Krishna conscious spiritual books. " +
                "Translate the following English text into Tamil. " +
                "The translation must reflect the author's mood and intent, maintaining the spiritual depth and devotional fervor found in classic Vaishnava literature. " +
                "Use appropriate Tamil spiritual terminology and ensure the tone is respectful and spiritually uplifting.";

        return webClient.post()
                .uri(baseUrl)
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
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String translatedText = (String) message.get("content");

                    Translation translation = new Translation();
                    translation.setSourceText(sourceText);
                    translation.setTranslatedText(translatedText);
                    translation.setSourceLanguage("English");
                    return translationRepository.save(translation);
                });
    }
}
