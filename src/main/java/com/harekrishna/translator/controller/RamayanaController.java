package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.RamayanaSaveRequest;
import com.harekrishna.translator.model.RamayanaSloka;
import com.harekrishna.translator.repository.RamayanaSlokaRepository;
import com.harekrishna.translator.service.SanskritTranslatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ramayana")
@RequiredArgsConstructor
public class RamayanaController {

    private final SanskritTranslatorService translatorService;
    private final RamayanaSlokaRepository ramayanaRepository;

    @PostMapping("/admin/save")
    public ResponseEntity<?> saveRamayanaSloka(@RequestBody RamayanaSaveRequest request) {
        try {
            var existingOpt = ramayanaRepository.findByCantoNumberAndChapterNumberAndVerseNumber(
                    request.getCantoNumber(), request.getChapterNumber(), request.getVerseNumber()
            );

            if (existingOpt.isPresent() && !request.isOverride()) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(Map.of("error", "Duplicate entry exists", "requiresOverride", true));
            }

            RamayanaSloka saved = translatorService.saveOrUpdateRamayanaSloka(request);
            return ResponseEntity.ok(Map.of("message", "Saved successfully", "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/toc")
    public ResponseEntity<?> getTableOfContents() {
        List<RamayanaSloka> all = ramayanaRepository.findAll();
        
        // Better structure: List of maps
        var toc = all.stream()
            .collect(Collectors.groupingBy(s -> s.getCantoNumber()))
            .entrySet().stream()
            .map(cantoEntry -> {
                Integer cantoNum = cantoEntry.getKey();
                String cantoName = cantoEntry.getValue().get(0).getCantoName();
                String cantoNameTa = cantoEntry.getValue().get(0).getCantoNameTa();
                
                var chapters = cantoEntry.getValue().stream()
                    .collect(Collectors.groupingBy(s -> s.getChapterNumber()))
                    .entrySet().stream()
                    .map(chapterEntry -> {
                        List<Integer> verses = chapterEntry.getValue().stream()
                            .map(RamayanaSloka::getVerseNumber)
                            .sorted()
                            .collect(Collectors.toList());
                        return Map.of(
                            "chapterNumber", chapterEntry.getKey(),
                            "verses", verses
                        );
                    })
                    .sorted((c1, c2) -> Integer.compare((Integer)c1.get("chapterNumber"), (Integer)c2.get("chapterNumber")))
                    .collect(Collectors.toList());
                    
                return Map.of(
                    "cantoNumber", cantoNum,
                    "cantoName", cantoName,
                    "cantoNameTa", cantoNameTa != null ? cantoNameTa : cantoName,
                    "chapters", chapters
                );
            })
            .sorted((c1, c2) -> Integer.compare((Integer)c1.get("cantoNumber"), (Integer)c2.get("cantoNumber")))
            .collect(Collectors.toList());

        return ResponseEntity.ok(toc);
    }

    @GetMapping("/{language}/{canto}/{chapter}/{verse}")
    public ResponseEntity<?> getSloka(
            @PathVariable String language,
            @PathVariable Integer canto,
            @PathVariable Integer chapter,
            @PathVariable Integer verse) {

        RamayanaSloka sloka = ramayanaRepository.findByCantoNumberAndChapterNumberAndVerseNumber(canto, chapter, verse)
                .orElse(null);

        if (sloka == null) {
            return ResponseEntity.notFound().build();
        }

        // Return language-specific response
        Map<String, Object> response;
        if ("tamil".equalsIgnoreCase(language)) {
            response = Map.of(
                "cantoName", sloka.getCantoNameTa() != null ? sloka.getCantoNameTa() : sloka.getCantoName(),
                "cantoNumber", sloka.getCantoNumber(),
                "chapterNumber", sloka.getChapterNumber(),
                "verseNumber", sloka.getVerseNumber(),
                "sanskritSloka", sloka.getSanskritSloka(),
                "transliteration", sloka.getTransliterationTa() != null ? sloka.getTransliterationTa() : "",
                "wordToWordMeaning", sloka.getWordToWordMeaningTa() != null ? sloka.getWordToWordMeaningTa() : "",
                "translation", sloka.getTranslationTa() != null ? sloka.getTranslationTa() : "",
                "purport", sloka.getPurportTa() != null ? sloka.getPurportTa() : ""
            );
        } else {
            response = Map.of(
                "cantoName", sloka.getCantoName(),
                "cantoNumber", sloka.getCantoNumber(),
                "chapterNumber", sloka.getChapterNumber(),
                "verseNumber", sloka.getVerseNumber(),
                "sanskritSloka", sloka.getSanskritSloka(),
                "transliteration", sloka.getTransliterationEn() != null ? sloka.getTransliterationEn() : "",
                "wordToWordMeaning", sloka.getWordToWordMeaningEn() != null ? sloka.getWordToWordMeaningEn() : "",
                "translation", sloka.getTranslationEn() != null ? sloka.getTranslationEn() : "",
                "purport", sloka.getPurportEn() != null ? sloka.getPurportEn() : ""
            );
        }

        return ResponseEntity.ok(response);
    }
}
