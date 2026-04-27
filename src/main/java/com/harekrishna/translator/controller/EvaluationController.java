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
        return evaluationService.evaluateTranslation(request.getEnglishText(), request.getTamilTranslation());
    }
}
