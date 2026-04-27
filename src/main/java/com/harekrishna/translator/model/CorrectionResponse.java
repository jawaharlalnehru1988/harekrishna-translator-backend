package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionResponse {
    private String suggestions;
    private String improvedTranslation;
}
