package com.harekrishna.translator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ramayana_slokas", indexes = {
    @Index(name = "idx_ramayana_hierarchy", columnList = "cantoNumber, chapterNumber, verseNumber")
})
public class RamayanaSloka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cantoName;     // e.g. "Bala Kanda"
    private String cantoNameTa;   // e.g. "பால காண்டம்"
    private Integer cantoNumber;  // e.g. 1
    private Integer chapterNumber;// e.g. 1
    private Integer verseNumber;  // e.g. 1

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sanskritSloka;

    // English fields
    @Lob
    @Column(columnDefinition = "TEXT")
    private String transliterationEn;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String wordToWordMeaningEn;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String translationEn;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String purportEn;

    // Tamil fields
    @Lob
    @Column(columnDefinition = "TEXT")
    private String transliterationTa;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String wordToWordMeaningTa;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String translationTa;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String purportTa;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
