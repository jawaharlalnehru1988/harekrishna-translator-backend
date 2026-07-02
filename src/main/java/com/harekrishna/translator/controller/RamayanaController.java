package com.harekrishna.translator.controller;

import com.harekrishna.translator.model.RamayanaSaveRequest;
import com.harekrishna.translator.model.RamayanaSloka;
import com.harekrishna.translator.repository.RamayanaSlokaRepository;
import com.harekrishna.translator.service.SanskritTranslatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSloka(
            @PathVariable String language,
            @PathVariable Integer canto,
            @PathVariable Integer chapter,
            @PathVariable Integer verse) {

        try {
            RamayanaSloka sloka = ramayanaRepository.findByCantoNumberAndChapterNumberAndVerseNumber(canto, chapter, verse)
                    .orElse(null);

            if (sloka == null) {
                return ResponseEntity.notFound().build();
            }

            // Return language-specific response
            Map<String, Object> response = new java.util.HashMap<>();
            if ("tamil".equalsIgnoreCase(language)) {
                response.put("cantoName", sloka.getCantoNameTa() != null ? sloka.getCantoNameTa() : sloka.getCantoName());
                response.put("cantoNumber", sloka.getCantoNumber());
                response.put("chapterNumber", sloka.getChapterNumber());
                response.put("verseNumber", sloka.getVerseNumber());
                response.put("sanskritSloka", sloka.getSanskritSloka());
                response.put("transliteration", sloka.getTransliterationTa() != null ? sloka.getTransliterationTa() : "");
                response.put("wordToWordMeaning", sloka.getWordToWordMeaningTa() != null ? sloka.getWordToWordMeaningTa() : "");
                response.put("translation", sloka.getTranslationTa() != null ? sloka.getTranslationTa() : "");
                response.put("purport", sloka.getPurportTa() != null ? sloka.getPurportTa() : "");
            } else {
                response.put("cantoName", sloka.getCantoName());
                response.put("cantoNumber", sloka.getCantoNumber());
                response.put("chapterNumber", sloka.getChapterNumber());
                response.put("verseNumber", sloka.getVerseNumber());
                response.put("sanskritSloka", sloka.getSanskritSloka());
                response.put("transliteration", sloka.getTransliterationEn() != null ? sloka.getTransliterationEn() : "");
                response.put("wordToWordMeaning", sloka.getWordToWordMeaningEn() != null ? sloka.getWordToWordMeaningEn() : "");
                response.put("translation", sloka.getTranslationEn() != null ? sloka.getTranslationEn() : "");
                response.put("purport", sloka.getPurportEn() != null ? sloka.getPurportEn() : "");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.toString(), "message", e.getMessage() != null ? e.getMessage() : "null"));
        }
    }
}
