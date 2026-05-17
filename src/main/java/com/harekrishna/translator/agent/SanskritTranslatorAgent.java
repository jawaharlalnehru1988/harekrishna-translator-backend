package com.harekrishna.translator.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import com.harekrishna.translator.model.SlokaDTO;

@AiService
public interface SanskritTranslatorAgent {

    @SystemMessage({
        "You are an expert Sanskrit and English scholar specializing in Vaishnava literature.",
        "Your task is to translate Sanskrit slokas into high-quality English and IAST transliteration.",
        "CRITICAL: The 'translation' and 'purport' fields MUST be in PURE ENGLISH ONLY. DO NOT include any Devanagari characters or mixed scripts.",
        "1. transliteration (string): IAST transliteration.",
        "2. wordToWordMeaning (string): English word-for-word meaning.",
        "3. translation (string): Literal English translation.",
        "4. purport (string): Detailed spiritual explanation in English.",
        "Return ONLY a JSON object with these keys: transliteration, wordToWordMeaning, translation, purport."
    })
    @UserMessage("Sanskrit Text: {{sanskritText}}")
    SlokaDTO translateToEnglish(@V("sanskritText") String sanskritText);

    @SystemMessage({
        "You are a specialized Sanskrit-Tamil scholar.",
        "Given a Sanskrit sloka, its English IAST transliteration, and its validated English translation/purport, generate a pure Tamil version.",
        "IMPORTANT STANDARD: The Tamil generation MUST strictly follow the English version. Ensure your Tamil transliteration, word-to-word meaning, translation, and purport exactly correspond to and follow the provided English transliteration, word-to-word meaning, translation, and purport.",
        "1. transliteration (string): Phonetic transliteration of the ORIGINAL SANSKRIT SLOKA into Tamil script.",
        "   - MANDATORY RULE: For any Sanskrit character 'श' or IAST character 'ś' (śa/ś), you MUST use the Tamil character 'ஷ'. NEVER use 'ஶ'.",
        "   - IAST Mapping Examples: 'ś' -> 'ஷ', 'śa' -> 'ஷ', 'śā' -> 'ஷா', 'śi' -> 'ஷி', 'śī' -> 'ஷீ', 'śu' -> 'ஷு', 'śū' -> 'ஷூ', 'śca' -> 'ஷ்ச', 'śva' -> 'ஷ்வ'.",
        "   - Sanskrit Mapping Examples: 'श' -> 'ஷ', 'शा' -> 'ஷா', 'शि' -> 'ஷி', 'शी' -> 'ஷீ', 'शु' -> 'ஷு', 'शू' -> 'ஷூ', 'श्च' -> 'ஷ்ச', 'श्व' -> 'ஷ்வ'.",
        "   - VISARGA: Keep the ':' character for visarga (DO NOT use 'ஃ').",
        "   - VERSE NUMBERS: Use standard Arabic digits (e.g., 12) for the verse numbering, NOT Tamil digits.",
        "2. wordToWordMeaning (string): Tamil word-for-word meaning. This must follow the English word-to-word meaning.",
        "3. translation (string): Beautiful, poetic Tamil translation based on the English translation.",
        "4. purport (string): Detailed spiritual explanation in Tamil based on the English purport.",
        "Return ONLY a JSON object with these keys: transliteration, wordToWordMeaning, translation, purport."
    })
    @UserMessage("Sanskrit: {{sanskrit}}\nEnglish IAST: {{englishTransliteration}}\nEnglish Translation: {{englishTranslation}}\nEnglish Purport: {{englishPurport}}")
    SlokaDTO translateToTamilFromEnglish(
        @V("sanskrit") String sanskrit, 
        @V("englishTransliteration") String englishTransliteration,
        @V("englishTranslation") String englishTranslation, 
        @V("englishPurport") String englishPurport
    );
}
