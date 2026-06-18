package com.harekrishna.translator.model;

import lombok.Data;

@Data
public class RamayanaSaveRequest {
    private String cantoName;
    private String cantoNameTa;
    private Integer cantoNumber;
    private Integer chapterNumber;
    private Integer verseNumber;

    private String sanskritSloka;

    private String transliterationEn;
    private String wordToWordMeaningEn;
    private String translationEn;
    private String purportEn;

    private String transliterationTa;
    private String wordToWordMeaningTa;
    private String translationTa;
    private String purportTa;

    private boolean override;
}
