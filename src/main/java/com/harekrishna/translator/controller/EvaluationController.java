package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.CorrectionRequest;
import com.harekrishna.translator.model.CorrectionResponse;
import com.harekrishna.translator.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/evaluate")
    public Mono<CorrectionResponse> evaluateTranslation(@RequestBody CorrectionRequest request) {
        if ((request.getEnglishText() != null && request.getEnglishText().length() > 3000) ||
            (request.getTamilTranslation() != null && request.getTamilTranslation().length() > 3000)) {
            throw new IllegalArgumentException("Text is too long. Please limit both English and Tamil versions to 3,000 characters to maintain accurate evaluation.");
        }
        return evaluationService.evaluateTranslation(request.getEnglishText(), request.getTamilTranslation());
    }
}
