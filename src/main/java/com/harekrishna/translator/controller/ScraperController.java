package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.ExtractionResponse;
import com.harekrishna.translator.service.WebScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final WebScraperService scraperService;
    private final com.harekrishna.translator.service.SanskritTranslatorService translatorService;

    @PostMapping("/extract")
    public ResponseEntity<?> extractFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body("URL is required");
        }
        
        try {
            ExtractionResponse response = scraperService.extractContextFromUrl(url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/translate-tamil")
    public ResponseEntity<?> translateToTamil(@RequestBody ExtractionResponse request) {
        try {
            ExtractionResponse response = translatorService.translateContextToTamil(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/generate-ramayana")
    public ResponseEntity<?> generateRamayanaEnglish(@RequestBody ExtractionResponse request) {
        try {
            ExtractionResponse response = translatorService.generateEnglishContextForRamayana(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
