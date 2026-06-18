package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResponse {
    private String slokaNumber;
    private String sanskritSloka;
    private String slokaTransliteration;
    private String wordToWordMeaning;
    private String translation;
    private String purport;
}
