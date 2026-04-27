package com.harekrishna.translator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String sourceText;

    @Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String translatedText;

    @Lob
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String correctedText;

    private String sourceLanguage;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isApproved;
}
