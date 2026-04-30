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
public class SanskritSloka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slokaNumber; // e.g. 1.1.1 or "Sloka 1"

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sanskritText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String transliteration;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String wordToWordMeaning;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String purport;

    private boolean isApproved = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
