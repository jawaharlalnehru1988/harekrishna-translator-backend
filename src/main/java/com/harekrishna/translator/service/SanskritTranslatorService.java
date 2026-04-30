package com.harekrishna.translator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harekrishna.translator.model.SanskritSloka;
import com.harekrishna.translator.model.SanskritSlokaDTO;
import com.harekrishna.translator.model.SanskritSlokaRequest;
import com.harekrishna.translator.repository.SanskritSlokaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SanskritTranslatorService {

    private final SanskritSlokaRepository sanskritSlokaRepository;
    private final GlossaryService glossaryService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.base-url}")
    private String baseUrl;

    public SanskritTranslatorService(SanskritSlokaRepository sanskritSlokaRepository, 
                                     GlossaryService glossaryService, 
                                     WebClient.Builder webClientBuilder,
                                     ObjectMapper objectMapper) {
        this.sanskritSlokaRepository = sanskritSlokaRepository;
        this.glossaryService = glossaryService;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Mono<SanskritSlokaDTO> translateSloka(SanskritSlokaRequest request) {
        String glossaryPrompt = glossaryService.getGlossaryPrompt();
        
        String systemPrompt = "You are an expert Sanskrit and Tamil scholar specializing in Vaishnava literature, specifically the style of Srila Prabhupada. " +
                "Your task is to translate a Sanskrit sloka into Tamil with the following structure: " +
                "1. slokaNumber (string): Use the provided one or identify it. " +
                "2. sanskritText (string): The original sloka. " +
                "3. transliteration (string): The Sanskrit sloka transliterated ONLY into Tamil script. Do NOT use English or Roman script here." +
                "4. wordToWordMeaning (string): Each word from the sloka transliterated into Tamil script, followed by its Tamil meaning, separated by semicolons (e.g., 'தப: - தவம்; ஸ்வாத்யாய - வேதங்களைப் படித்தல்'). Do NOT use original Sanskrit script here; use Tamil transliteration for the source words." +
                "5. purport (string): A high-quality Tamil translation/explanation. " +
                "\n\nCRITICAL: Return the response ONLY as a clean JSON object. Do not use markdown blocks. Ensure all values are strings." +
                glossaryPrompt;

        String userPrompt = String.format("Sloka Number: %s\nSanskrit Text: %s", 
                (request.getSlokaNumber() != null && !request.getSlokaNumber().isEmpty()) ? request.getSlokaNumber() : "Not provided", 
                request.getSanskritText());

        return webClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "response_format", Map.of("type", "json_object"),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        String content = (String) message.get("content");
                        return objectMapper.readValue(content, SanskritSlokaDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse LLM response: " + e.getMessage(), e);
                    }
                });
    }

    public SanskritSloka saveOrUpdateSloka(SanskritSlokaDTO dto) {
        // Logic to either create new or update existing if slokaNumber matches (could be improved)
        SanskritSloka sloka = new SanskritSloka();
        sloka.setSlokaNumber(dto.getSlokaNumber());
        sloka.setSanskritText(dto.getSanskritText());
        sloka.setTransliteration(dto.getTransliteration());
        sloka.setWordToWordMeaning(dto.getWordToWordMeaning());
        sloka.setPurport(dto.getPurport());
        sloka.setApproved(true);
        sloka.setUpdatedAt(LocalDateTime.now());
        return sanskritSlokaRepository.save(sloka);
    }
    
    public List<SanskritSloka> getAllSavedSlokas() {
        return sanskritSlokaRepository.findAll();
    }
}
