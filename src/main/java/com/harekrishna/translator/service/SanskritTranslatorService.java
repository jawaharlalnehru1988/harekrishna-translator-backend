package com.harekrishna.translator.service;

import com.harekrishna.translator.agent.SanskritTranslatorAgent;
import com.harekrishna.translator.model.*;
import com.harekrishna.translator.repository.ScriptureRepository;
import com.harekrishna.translator.repository.SlokaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SanskritTranslatorService {

    private final SlokaRepository slokaRepository;
    private final ScriptureRepository scriptureRepository;
    private final SanskritTranslatorAgent translatorAgent;

    // Pattern to detect Latin/English characters
    private static final Pattern LATIN_PATTERN = Pattern.compile("[a-zA-Z]");

    public SanskritTranslatorService(SlokaRepository slokaRepository, 
                                     ScriptureRepository scriptureRepository,
                                     SanskritTranslatorAgent translatorAgent) {
        this.slokaRepository = slokaRepository;
        this.scriptureRepository = scriptureRepository;
        this.translatorAgent = translatorAgent;
    }

    public Mono<SlokaDTO> translateSloka(SlokaRequest request) {
        return Mono.fromCallable(() -> {
            Scripture scripture = scriptureRepository.findById(request.getScriptureId())
                    .orElseThrow(() -> new RuntimeException("Scripture not found"));

            // STEP 1: Node English - Generate high-quality English foundation
            SlokaDTO englishResult = translatorAgent.translateToEnglish(request.getSanskritText());
            
            SlokaDTO finalDto = new SlokaDTO();
            finalDto.setScriptureId(request.getScriptureId());
            finalDto.setScriptureTitle(scripture.getTitle());
            finalDto.setMajorDivision(request.getMajorDivision());
            finalDto.setMinorDivision(request.getMinorDivision());
            finalDto.setVerseNumber(request.getVerseNumber());
            finalDto.setSanskritText(request.getSanskritText());
            
            // Set English fields
            finalDto.setTransliterationEn(englishResult.getTransliteration());
            finalDto.setWordToWordMeaningEn(englishResult.getWordToWordMeaning());
            finalDto.setTranslationEn(englishResult.getTranslation());
            finalDto.setPurportEn(englishResult.getPurport());

            // STEP 2: Node Tamil - Generate Tamil from English + Sanskrit
            if ("TAMIL".equalsIgnoreCase(request.getTargetLanguage())) {
                String enTranslation = englishResult.getTranslation() != null ? englishResult.getTranslation() : "";
                String enPurport = englishResult.getPurport() != null ? englishResult.getPurport() : "No purport requested.";

                SlokaDTO tamilResult = translatorAgent.translateToTamilFromEnglish(
                    request.getSanskritText(), 
                    enTranslation, 
                    enPurport
                );

                // STEP 3: Safe Boundary Check (Max 1 Retry)
                if (containsLatin(tamilResult.getTransliteration()) || containsLatin(tamilResult.getTranslation())) {
                    System.out.println("Script leakage detected. Triggering self-correction node...");
                    
                    tamilResult = translatorAgent.translateToTamilFromEnglish(
                        request.getSanskritText(), 
                        enTranslation, 
                        "STRICT WARNING: PREVIOUS ATTEMPT FAILED. DO NOT USE LATIN CHARACTERS. REFERENCE: " + enPurport
                    );
                }

                finalDto.setTransliteration(tamilResult.getTransliteration());
                finalDto.setWordToWordMeaning(tamilResult.getWordToWordMeaning());
                finalDto.setTranslation(tamilResult.getTranslation());
                finalDto.setPurport(tamilResult.getPurport());
            }

            return finalDto;
        });
    }

    private boolean containsLatin(String text) {
        if (text == null) return false;
        return LATIN_PATTERN.matcher(text).find();
    }

    public Sloka saveOrUpdateSloka(SlokaDTO dto) {
        Sloka sloka = slokaRepository.findByScriptureId(dto.getScriptureId()).stream()
                .filter(s -> s.getMajorDivision().equals(dto.getMajorDivision()) && 
                            s.getMinorDivision().equals(dto.getMinorDivision()) && 
                            s.getVerseNumber().equals(dto.getVerseNumber()))
                .findFirst()
                .orElse(new Sloka());

        if (sloka.getId() == null) {
            Scripture scripture = scriptureRepository.findById(dto.getScriptureId())
                    .orElseThrow(() -> new RuntimeException("Scripture not found"));
            sloka.setScripture(scripture);
            sloka.setMajorDivision(dto.getMajorDivision());
            sloka.setMinorDivision(dto.getMinorDivision());
            sloka.setVerseNumber(dto.getVerseNumber());
            sloka.setSanskritText(dto.getSanskritText());
            sloka.setCreatedAt(LocalDateTime.now());
        }

        if (dto.getTranslation() != null) sloka.setTranslation(dto.getTranslation());
        if (dto.getPurport() != null) sloka.setPurport(dto.getPurport());
        if (dto.getTransliteration() != null) sloka.setTransliteration(dto.getTransliteration());
        if (dto.getWordToWordMeaning() != null) sloka.setWordToWordMeaning(dto.getWordToWordMeaning());

        if (dto.getTranslationEn() != null) sloka.setTranslationEn(dto.getTranslationEn());
        if (dto.getPurportEn() != null) sloka.setPurportEn(dto.getPurportEn());
        if (dto.getTransliterationEn() != null) sloka.setTransliterationEn(dto.getTransliterationEn());
        if (dto.getWordToWordMeaningEn() != null) sloka.setWordToWordMeaningEn(dto.getWordToWordMeaningEn());

        sloka.setApproved(true);
        sloka.setUpdatedAt(LocalDateTime.now());
        return slokaRepository.save(sloka);
    }
    
    public List<Sloka> getAllSavedSlokas() {
        return slokaRepository.findAll();
    }
}
