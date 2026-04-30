package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanskritSlokaDTO {
    private String slokaNumber;
    private String sanskritText;
    private String transliteration;
    private String wordToWordMeaning;
    private String purport;
}
