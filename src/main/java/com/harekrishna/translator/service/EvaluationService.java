package com.harekrishna.translator.service;

import com.harekrishna.translator.model.CorrectionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {

    private final WebClient webClient;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.base-url}")
    private String baseUrl;

    public EvaluationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<CorrectionResponse> evaluateTranslation(String englishText, String tamilTranslation) {
        String systemPrompt = "You are an expert evaluator of translations specializing in Krishna conscious spiritual books. " +
                "Your task is to compare the original English text with the provided Tamil translation. " +
                "Analyze the accuracy, word choice, and whether the translation is comfortable for modern Tamil readers while maintaining the author's original intent and spiritual depth. " +
                "Only provide suggestions if the translation accuracy is missed or can be significantly improved. Do not provide sentence-by-sentence commentary if it is already good. " +
                "Respond in JSON format with exactly two fields:\n" +
                "1. 'suggestions': Your feedback and suggested improvements.\n" +
                "2. 'improvedTranslation': Your suggested improved version of the translation (if no improvements are needed, simply return the original Tamil translation provided).\n" +
                "Ensure the response is valid JSON.";

        String userPrompt = "Original English Text: " + englishText + "\n" +
                "Provided Tamil Translation: " + tamilTranslation;

        return webClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        ),
                        "response_format", Map.of("type", "json_object")
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");

                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        return mapper.readValue(content, CorrectionResponse.class);
                    } catch (Exception e) {
                        return new CorrectionResponse("Error parsing AI response: " + e.getMessage(), tamilTranslation);
                    }
                });
    }
}
