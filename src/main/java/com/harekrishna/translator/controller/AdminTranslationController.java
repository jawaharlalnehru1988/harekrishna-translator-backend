package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.IngestionType;
import com.harekrishna.translator.service.ExcelIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminTranslationController {

    private final ExcelIngestionService ingestionService;

    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") IngestionType type,
            @RequestParam(value = "scripture", defaultValue = "GENERAL") String scriptureContext) {
        
        try {
            String jobId = ingestionService.ingestExcel(file.getInputStream(), type, scriptureContext);
            return ResponseEntity.ok(Map.of("jobId", jobId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/upload-status/{jobId}")
    public ResponseEntity<?> getUploadStatus(@PathVariable String jobId) {
        var status = ingestionService.getUploadStatus(jobId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> saveFeedback(@RequestBody com.harekrishna.translator.model.FeedbackRequest request) {
        try {
            ingestionService.saveFeedback(request.getEnglishText(), request.getCorrectedTamilText(), request.getType(), request.getScriptureContext() != null ? request.getScriptureContext() : "GENERAL");
            return ResponseEntity.ok("Feedback saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
