package com.harekrishna.translator.service;

import com.harekrishna.translator.agent.SanskritTranslatorAgent;
import com.harekrishna.translator.model.*;
import com.harekrishna.translator.repository.ScriptureRepository;
import com.harekrishna.translator.repository.SlokaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SanskritTranslatorService {

    private final SlokaRepository slokaRepository;
    private final ScriptureRepository scriptureRepository;
    private final SanskritTranslatorAgent translatorAgent;
    private final com.harekrishna.translator.repository.RamayanaSlokaRepository ramayanaSlokaRepository;

    // Pattern to detect Latin/English characters
    private static final Pattern LATIN_PATTERN = Pattern.compile("[a-zA-Z]");
    // Pattern to detect Devanagari (Sanskrit/Hindi) characters
    private static final Pattern DEVANAGARI_PATTERN = Pattern.compile("[\\u0900-\\u097F]");
    // Pattern to detect the old Tamil character 'ஶ' (Grantha sha)
    private static final Pattern OLD_TAMIL_SHA_PATTERN = Pattern.compile("\\u0BB6");

    public SanskritTranslatorService(SlokaRepository slokaRepository, 
                                     ScriptureRepository scriptureRepository,
                                     SanskritTranslatorAgent translatorAgent,
                                     com.harekrishna.translator.repository.RamayanaSlokaRepository ramayanaSlokaRepository) {
        this.slokaRepository = slokaRepository;
        this.scriptureRepository = scriptureRepository;
        this.translatorAgent = translatorAgent;
        this.ramayanaSlokaRepository = ramayanaSlokaRepository;
    }

    public Mono<SlokaDTO> translateSloka(SlokaRequest request) {
        return Mono.fromCallable(() -> {
            Scripture scripture = scriptureRepository.findById(request.getScriptureId())
                    .orElseThrow(() -> new RuntimeException("Scripture not found"));

            // STEP 1: Node English - Generate high-quality English foundation
            SlokaDTO englishResult = translatorAgent.translateToEnglish(request.getSanskritText());
            
            // Validate English results for Devanagari script leakage
            if (containsDevanagari(englishResult.getTranslation()) || containsDevanagari(englishResult.getPurport())) {
                System.out.println("Devanagari leakage detected in English results. Triggering self-correction...");
                englishResult = translatorAgent.translateToEnglish(
                    request.getSanskritText() + "\nSTRICT: Use ONLY English characters in translation and purport. NO DEVANAGARI."
                );
            }
            
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

                String slokaRef = scripture.getTitle() + " " + request.getMajorDivision() + "." + request.getMinorDivision() + "." + request.getVerseNumber();

                SlokaDTO tamilResult = translatorAgent.translateToTamilFromEnglish(
                    scripture.getTitle(),
                    slokaRef,
                    request.getSanskritText(), 
                    englishResult.getTransliteration(),
                    englishResult.getWordToWordMeaning(),
                    enTranslation, 
                    enPurport
                );

                // STEP 3: Safe Boundary Check (Max 1 Retry)
                if (containsLatin(tamilResult.getTransliteration()) || 
                    containsLatin(tamilResult.getTranslation()) ||
                    containsOldTamilSha(tamilResult.getTransliteration())) {
                    
                    String warning = "STRICT WARNING: PREVIOUS ATTEMPT FAILED. ";
                    if (containsOldTamilSha(tamilResult.getTransliteration())) {
                        warning += "DO NOT USE THE CHARACTER 'ஶ'. USE 'ஷ' INSTEAD. ";
                    }
                    if (containsLatin(tamilResult.getTransliteration()) || containsLatin(tamilResult.getTranslation())) {
                        warning += "DO NOT USE LATIN CHARACTERS. ";
                    }

                    System.out.println("Tamil script leakage or old character detected. Triggering self-correction node...");
                    
                    tamilResult = translatorAgent.translateToTamilFromEnglish(
                        scripture.getTitle(),
                        slokaRef,
                        request.getSanskritText(), 
                        englishResult.getTransliteration(),
                        englishResult.getWordToWordMeaning(),
                        enTranslation, 
                        warning + "REFERENCE: " + enPurport
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

    private boolean containsDevanagari(String text) {
        if (text == null) return false;
        return DEVANAGARI_PATTERN.matcher(text).find();
    }

    private boolean containsOldTamilSha(String text) {
        if (text == null) return false;
        return OLD_TAMIL_SHA_PATTERN.matcher(text).find();
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

    public ExtractionResponse translateContextToTamil(ExtractionResponse englishContext) {
        SlokaDTO tamilResult = translatorAgent.translateToTamilFromEnglish(
                "Ramayana", // Assuming Extraction API is primarily used for Ramayana as requested
                englishContext.getSlokaNumber(),
                englishContext.getSanskritSloka(),
                englishContext.getSlokaTransliteration(),
                englishContext.getWordToWordMeaning(),
                englishContext.getTranslation(),
                englishContext.getPurport()
        );

        if (containsLatin(tamilResult.getTransliteration()) || 
            containsLatin(tamilResult.getTranslation()) ||
            containsOldTamilSha(tamilResult.getTransliteration())) {
            
            String warning = "STRICT WARNING: PREVIOUS ATTEMPT FAILED. ";
            if (containsOldTamilSha(tamilResult.getTransliteration())) {
                warning += "DO NOT USE THE CHARACTER 'ஶ'. USE 'ஷ' INSTEAD. ";
            }
            if (containsLatin(tamilResult.getTransliteration()) || containsLatin(tamilResult.getTranslation())) {
                warning += "DO NOT USE LATIN CHARACTERS. ";
            }

            System.out.println("Tamil script leakage or old character detected in Extractor API. Triggering self-correction node...");
            
            tamilResult = translatorAgent.translateToTamilFromEnglish(
                "Ramayana", // Assuming Extraction API is primarily used for Ramayana
                englishContext.getSlokaNumber(),
                englishContext.getSanskritSloka(),
                englishContext.getSlokaTransliteration(),
                englishContext.getWordToWordMeaning(),
                englishContext.getTranslation(),
                warning + "REFERENCE: " + englishContext.getPurport()
            );
        }

        ExtractionResponse response = new ExtractionResponse();
        response.setSlokaNumber(tamilResult.getSlokaNumber() != null ? tamilResult.getSlokaNumber() : englishContext.getSlokaNumber());
        response.setSanskritSloka(englishContext.getSanskritSloka());
        response.setSlokaTransliteration(tamilResult.getTransliteration());
        response.setWordToWordMeaning(tamilResult.getWordToWordMeaning());
        response.setTranslation(tamilResult.getTranslation());
        response.setPurport(tamilResult.getPurport());

        return response;
    }

    public ExtractionResponse generateEnglishContextForRamayana(ExtractionResponse payload) {
        // Assume generatePurport is true unless user explicitly disables it, 
        // wait the user said "purport(optional by default disabled)". So we should read a boolean or just default to false if purport is empty.
        // We will default generatePurport to false unless payload explicitly says otherwise, or we can just look if they passed an instruction.
        // Let's check if the payload's purport is "GENERATE". If so, generate it. Otherwise don't.
        boolean generatePurport = "GENERATE".equalsIgnoreCase(payload.getPurport());

        SlokaDTO englishResult = translatorAgent.generateRamayanaEnglish(
                payload.getSanskritSloka(),
                payload.getSlokaTransliteration(),
                generatePurport
        );

        ExtractionResponse response = new ExtractionResponse();
        response.setSlokaNumber(payload.getSlokaNumber());
        response.setSanskritSloka(payload.getSanskritSloka());
        response.setSlokaTransliteration(payload.getSlokaTransliteration());
        response.setWordToWordMeaning(englishResult.getWordToWordMeaning());
        response.setTranslation(englishResult.getTranslation());
        response.setPurport(englishResult.getPurport());

        return response;
    }

    @Transactional
    public RamayanaSloka saveOrUpdateRamayanaSloka(RamayanaSaveRequest request) {
        RamayanaSloka sloka = this.ramayanaSlokaRepository.findByCantoNumberAndChapterNumberAndVerseNumber(
                request.getCantoNumber(), request.getChapterNumber(), request.getVerseNumber()
        ).orElse(new RamayanaSloka());

        sloka.setCantoName(request.getCantoName());
        sloka.setCantoNameTa(request.getCantoNameTa());
        sloka.setCantoNumber(request.getCantoNumber());
        sloka.setChapterNumber(request.getChapterNumber());
        sloka.setVerseNumber(request.getVerseNumber());
        sloka.setSanskritSloka(request.getSanskritSloka());

        sloka.setTransliterationEn(request.getTransliterationEn());
        sloka.setWordToWordMeaningEn(request.getWordToWordMeaningEn());
        sloka.setTranslationEn(request.getTranslationEn());
        sloka.setPurportEn(request.getPurportEn());

        sloka.setTransliterationTa(request.getTransliterationTa());
        sloka.setWordToWordMeaningTa(request.getWordToWordMeaningTa());
        sloka.setTranslationTa(request.getTranslationTa());
        sloka.setPurportTa(request.getPurportTa());

        sloka.setUpdatedAt(java.time.LocalDateTime.now());
        if (sloka.getId() == null) {
            sloka.setCreatedAt(java.time.LocalDateTime.now());
        }

        return this.ramayanaSlokaRepository.save(sloka);
    }
}
