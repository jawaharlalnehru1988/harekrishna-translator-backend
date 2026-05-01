package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlokaDTO {
    private Long id;
    private Long scriptureId;
    private String scriptureTitle;
    private Integer majorDivision;
    private Integer minorDivision;
    private Integer verseNumber;
    
    private String sanskritText;
    private String transliteration;
    private String wordToWordMeaning;
    private String translation;
    private String purport;
}
