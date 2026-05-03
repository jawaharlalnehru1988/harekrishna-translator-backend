package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.Translation;
import com.harekrishna.translator.repository.TranslationRepository;
import com.harekrishna.translator.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private TranslationService translationService;

    @GetMapping
    public List<Translation> getAllTranslations() {
        return translationRepository.findAll();
    }

    /**
     * Endpoint for batch translation calls. Returns just the string.
     */
    @PostMapping("/batch")
    public Mono<String> translateBatch(@RequestBody Map<String, String> request) {
        String sourceText = request.get("sourceText");
        String sourceLang = request.getOrDefault("sourceLanguage", "English");
        String targetLang = request.getOrDefault("targetLanguage", "Tamil");
        
        return translationService.translateOnly(sourceText, sourceLang, targetLang);
    }

    /**
     * Endpoint to save a final combined translation to history.
     */
    @PostMapping("/save")
    public Translation saveTranslation(@RequestBody Translation translation) {
        return translationService.save(translation);
    }

    @PutMapping("/{id}")
    public Translation updateTranslation(@PathVariable Long id, @RequestBody Translation translationDetails) {
        Translation translation = translationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Translation not found for id: " + id));
        
        translation.setCorrectedText(translationDetails.getCorrectedText());
        translation.setApproved(translationDetails.isApproved());
        
        return translationRepository.save(translation);
    }
}
