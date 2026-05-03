package com.harekrishna.translator.model;

import lombok.Data;

@Data
public class SlokaRequest {
    private Long scriptureId;
    private Integer majorDivision;
    private Integer minorDivision;
    private Integer verseNumber;
    private String sanskritText;
    private String targetLanguage; // "TAMIL" or "ENGLISH"
    private boolean includePurport = true;
}
