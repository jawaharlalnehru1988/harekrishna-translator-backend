package com.harekrishna.translator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harekrishna.translator.model.*;
import com.harekrishna.translator.repository.ScriptureRepository;
import com.harekrishna.translator.repository.SlokaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SanskritTranslatorService {

    private final SlokaRepository slokaRepository;
    private final ScriptureRepository scriptureRepository;
    private final GlossaryService glossaryService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.base-url}")
    private String baseUrl;

    public SanskritTranslatorService(SlokaRepository slokaRepository, 
                                     ScriptureRepository scriptureRepository,
                                     GlossaryService glossaryService, 
                                     WebClient.Builder webClientBuilder,
                                     ObjectMapper objectMapper) {
        this.slokaRepository = slokaRepository;
        this.scriptureRepository = scriptureRepository;
        this.glossaryService = glossaryService;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Mono<SlokaDTO> translateSloka(SlokaRequest request) {
        String glossaryPrompt = glossaryService.getGlossaryPrompt();
        
        Scripture scripture = scriptureRepository.findById(request.getScriptureId())
                .orElseThrow(() -> new RuntimeException("Scripture not found with ID: " + request.getScriptureId()));

        String systemPrompt = String.format(
                "You are an expert Sanskrit and Tamil scholar specializing in Vaishnava literature. " +
                "Translate the following sloka from '%s'. " +
                "The hierarchy is %s: %d, %s: %d, Verse: %d. " +
                "1. transliteration (string): Tamil script transliteration. " +
                "2. wordToWordMeaning (string): Tamil word-for-word meaning (Tamil transliteration - Tamil meaning;). " +
                "3. translation (string): Literal Tamil translation. " +
                "4. purport (string): Detailed spiritual explanation in Srila Prabhupada's style. " +
                "\n\nReturn ONLY a JSON object with these keys: transliteration, wordToWordMeaning, translation, purport.",
                scripture.getTitle(), scripture.getMajorDivisionName(), request.getMajorDivision(), 
                scripture.getMinorDivisionName(), request.getMinorDivision(), request.getVerseNumber());

        String userPrompt = String.format("Sanskrit Text: %s", request.getSanskritText());

        return webClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "response_format", Map.of("type", "json_object"),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt + glossaryPrompt),
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
                        SlokaDTO dto = objectMapper.readValue(content, SlokaDTO.class);
                        
                        // Set metadata from request
                        dto.setScriptureId(request.getScriptureId());
                        dto.setScriptureTitle(scripture.getTitle());
                        dto.setMajorDivision(request.getMajorDivision());
                        dto.setMinorDivision(request.getMinorDivision());
                        dto.setVerseNumber(request.getVerseNumber());
                        dto.setSanskritText(request.getSanskritText());
                        
                        return dto;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse LLM response: " + e.getMessage(), e);
                    }
                });
    }

    public Sloka saveOrUpdateSloka(SlokaDTO dto) {
        Scripture scripture = scriptureRepository.findById(dto.getScriptureId())
                .orElseThrow(() -> new RuntimeException("Scripture not found"));

        Sloka sloka = new Sloka();
        sloka.setScripture(scripture);
        sloka.setMajorDivision(dto.getMajorDivision());
        sloka.setMinorDivision(dto.getMinorDivision());
        sloka.setVerseNumber(dto.getVerseNumber());
        sloka.setSanskritText(dto.getSanskritText());
        sloka.setTransliteration(dto.getTransliteration());
        sloka.setWordToWordMeaning(dto.getWordToWordMeaning());
        sloka.setTranslation(dto.getTranslation());
        sloka.setPurport(dto.getPurport());
        sloka.setApproved(true);
        sloka.setUpdatedAt(LocalDateTime.now());
        return slokaRepository.save(sloka);
    }
    
    public List<Sloka> getAllSavedSlokas() {
        return slokaRepository.findAll();
    }
}
