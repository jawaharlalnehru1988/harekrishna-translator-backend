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
@Table(indexes = {
    @Index(name = "idx_sloka_hierarchy", columnList = "scripture_id, majorDivision, minorDivision, verseNumber")
})
public class Sloka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scripture_id", nullable = false)
    private Scripture scripture;

    private Integer majorDivision; // e.g., 1 (Bala Kanda)
    private Integer minorDivision; // e.g., 1 (Sarga 1)
    private Integer verseNumber;   // e.g., 1

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
    private String translation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String purport;

    private boolean isApproved = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
