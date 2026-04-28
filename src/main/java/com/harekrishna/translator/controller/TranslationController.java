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

    @PostMapping
    public Mono<Translation> createTranslation(@RequestBody Map<String, String> request) {
        String sourceText = request.get("sourceText");
        
        if (sourceText != null && sourceText.length() > 3000) {
            throw new IllegalArgumentException("Text is too long. Please limit your input to 3,000 characters for the best spiritual accuracy.");
        }
        
        return translationService.translate(sourceText);
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
