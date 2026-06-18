package com.harekrishna.translator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harekrishna.translator.model.ExtractionResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebScraperService {

    public ExtractionResponse extractContextFromUrl(String url) {
        log.info("Extracting context from URL: {}", url);
        try {
            if (url.contains("prabhupada.io")) {
                return extractFromPrabhupadaIo(url);
            }
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(20000)
                    .get();

            String slokaNumber = extractSlokaNumber(doc);
            
            // Try vedabase specific classes first
            String sanskritSloka = extractContentWithoutH2(doc, ".av-devanagari");
            String transliteration = extractContentWithoutH2(doc, ".av-verse_text");
            String wordToWord = extractContentWithoutH2(doc, ".av-synonyms");
            String translation = extractContentWithoutH2(doc, ".av-translation");
            String purport = extractContentWithoutH2(doc, ".av-purport");
            
            // Fallback for old vedabase or prabhupada.io if they use different classes
            if (sanskritSloka.isEmpty() && transliteration.isEmpty()) {
                sanskritSloka = extractTextByClass(doc, "r-deva", "r-verse-text");
                transliteration = extractTextByClass(doc, "r-verse-text");
                wordToWord = extractTextByClass(doc, "r-synonyms");
                translation = extractTextByClass(doc, "r-translation");
                purport = doc.select(".wrapper-purport .r-paragraph, .r-purport .r-paragraph, .r-purport")
                        .stream()
                        .map(this::extractText)
                        .filter(text -> !text.isEmpty() && !text.startsWith("Purport"))
                        .collect(Collectors.joining("\n\n"));
            }

            return ExtractionResponse.builder()
                    .slokaNumber(slokaNumber)
                    .sanskritSloka(sanskritSloka)
                    .slokaTransliteration(transliteration)
                    .wordToWordMeaning(wordToWord)
                    .translation(translation)
                    .purport(purport)
                    .build();

        } catch (IOException e) {
            log.error("Failed to extract context from URL: {}", url, e);
            throw new RuntimeException("Failed to extract context from URL: " + e.getMessage());
        }
    }

    private ExtractionResponse extractFromPrabhupadaIo(String url) throws IOException {
        Pattern pattern = Pattern.compile("/([^/]+)/(\\d+)/([^/]+)/?$");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid prabhupada.io URL format. Expected .../book/chapter/verse");
        }
        
        String book = matcher.group(1);
        String chapter = matcher.group(2);
        String verseId = matcher.group(3);
        
        String apiUrl = "https://prabhupada.io/content/" + book + "/" + chapter + ".json";
        log.info("Fetching JSON from: {}", apiUrl);
        
        String jsonStr = Jsoup.connect(apiUrl)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .ignoreContentType(true)
                .execute()
                .body();
                
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonStr);
        JsonNode verses = root.path("verses");
        
        for (JsonNode verse : verses) {
            String vId = verse.path("id").asText();
            boolean matches = false;
            
            if (vId.equals(verseId)) {
                matches = true;
            } else if (vId.contains("-")) {
                String[] parts = vId.split("-");
                if (parts.length == 2) {
                    try {
                        int start = Integer.parseInt(parts[0]);
                        int end = Integer.parseInt(parts[1]);
                        int target = Integer.parseInt(verseId);
                        if (target >= start && target <= end) {
                            matches = true;
                        }
                    } catch (Exception ignored) {}
                }
            }
            
            if (matches) {
                String slokaNumber = verse.path("label").asText();
                String sanskritSloka = verse.path("devanagari").asText();
                String transliteration = verse.path("transliteration").asText();
                String wordToWord = verse.path("synonyms").asText().replace("*", "");
                String translation = verse.path("translation").asText();
                String purport = verse.path("purport").asText().replace("*", "");
                
                return ExtractionResponse.builder()
                        .slokaNumber(slokaNumber)
                        .sanskritSloka(sanskritSloka)
                        .slokaTransliteration(transliteration)
                        .wordToWordMeaning(wordToWord)
                        .translation(translation)
                        .purport(purport)
                        .build();
            }
        }
        throw new RuntimeException("Verse " + verseId + " not found in chapter " + chapter);
    }

    private String extractContentWithoutH2(Document doc, String selector) {
        Element el = doc.select(selector).first();
        if (el == null) return "";
        // Remove H2 if it exists inside this wrapper
        el.select("h2").remove();
        return extractText(el);
    }

    private String extractSlokaNumber(Document doc) {
        Element titleEl = doc.select("h1").first();
        if (titleEl != null) {
            return titleEl.text().trim();
        }
        return "";
    }

    private String extractTextByClass(Document doc, String... classNames) {
        for (String className : classNames) {
            Element el = doc.select("." + className).first();
            if (el != null && !el.text().trim().isEmpty()) {
                return extractText(el);
            }
        }
        return "";
    }

    private String extractText(Element el) {
        if (el == null) return "";
        // Replace <br> with newlines for better formatting
        el.select("br").append("\\n");
        return el.text().replace("\\n", "\n").trim();
    }
}
