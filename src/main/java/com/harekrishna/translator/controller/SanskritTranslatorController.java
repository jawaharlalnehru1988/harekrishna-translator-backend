package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.SanskritSloka;
import com.harekrishna.translator.model.SanskritSlokaDTO;
import com.harekrishna.translator.model.SanskritSlokaRequest;
import com.harekrishna.translator.service.SanskritTranslatorService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/translator/sanskrit")
public class SanskritTranslatorController {

    private final SanskritTranslatorService sanskritTranslatorService;

    public SanskritTranslatorController(SanskritTranslatorService sanskritTranslatorService) {
        this.sanskritTranslatorService = sanskritTranslatorService;
    }

    @PostMapping("/translate")
    public Mono<SanskritSlokaDTO> translate(@RequestBody SanskritSlokaRequest request) {
        return sanskritTranslatorService.translateSloka(request);
    }

    @PostMapping("/save")
    public SanskritSloka save(@RequestBody SanskritSlokaDTO dto) {
        return sanskritTranslatorService.saveOrUpdateSloka(dto);
    }

    @GetMapping("/history")
    public List<SanskritSloka> getHistory() {
        return sanskritTranslatorService.getAllSavedSlokas();
    }
}
