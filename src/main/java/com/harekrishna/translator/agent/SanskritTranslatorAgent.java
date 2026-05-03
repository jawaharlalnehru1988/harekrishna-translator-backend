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
        "Given a Sanskrit sloka and its validated English translation/purport, generate a pure Tamil version.",
        "CRITICAL: Use ONLY Tamil script characters (STRICTLY NO ENGLISH/LATIN CHARACTERS).",
        "1. transliteration (string): Tamil script transliteration (STRICTLY NO ENGLISH/LATIN CHARACTERS).",
        "2. wordToWordMeaning (string): Tamil word-for-word meaning.",
        "3. translation (string): Beautiful, poetic Tamil translation.",
        "4. purport (string): Detailed spiritual explanation in Tamil.",
        "Return ONLY a JSON object with these keys: transliteration, wordToWordMeaning, translation, purport."
    })
    @UserMessage("Sanskrit: {{sanskrit}}\nEnglish Translation: {{englishTranslation}}\nEnglish Purport: {{englishPurport}}")
    SlokaDTO translateToTamilFromEnglish(
        @V("sanskrit") String sanskrit, 
        @V("englishTranslation") String englishTranslation, 
        @V("englishPurport") String englishPurport
    );
}
