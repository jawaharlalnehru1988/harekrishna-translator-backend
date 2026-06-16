package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.IngestionType;
import com.harekrishna.translator.service.ExcelIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminTranslationController {

    private final ExcelIngestionService ingestionService;

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") IngestionType type) {
        
        try {
            ingestionService.ingestExcel(file.getInputStream(), type);
            return ResponseEntity.ok("Successfully ingested " + type + " data.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> saveFeedback(@RequestBody com.harekrishna.translator.model.FeedbackRequest request) {
        try {
            ingestionService.saveFeedback(request.getEnglishText(), request.getCorrectedTamilText(), request.getType());
            return ResponseEntity.ok("Feedback saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
