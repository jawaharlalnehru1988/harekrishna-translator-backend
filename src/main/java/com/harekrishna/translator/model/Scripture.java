package com.harekrishna.translator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scripture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title; // e.g., "Ramayana"

    private String author; // e.g., "Valmiki"

    private String majorDivisionName; // e.g., "Kanda", "Parva", "Canto"
    private String minorDivisionName; // e.g., "Sarga", "Adhyaya", "Chapter"

    @OneToMany(mappedBy = "scripture", cascade = CascadeType.ALL)
    private List<Sloka> slokas;
}
