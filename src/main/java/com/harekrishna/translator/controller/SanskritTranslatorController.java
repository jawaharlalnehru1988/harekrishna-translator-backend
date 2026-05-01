package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.Sloka;
import com.harekrishna.translator.model.SlokaDTO;
import com.harekrishna.translator.model.SlokaRequest;
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
    public Mono<SlokaDTO> translate(@RequestBody SlokaRequest request) {
        return sanskritTranslatorService.translateSloka(request);
    }

    @PostMapping("/save")
    public Sloka save(@RequestBody SlokaDTO dto) {
        return sanskritTranslatorService.saveOrUpdateSloka(dto);
    }

    @GetMapping("/history")
    public List<Sloka> getHistory() {
        return sanskritTranslatorService.getAllSavedSlokas();
    }
}
