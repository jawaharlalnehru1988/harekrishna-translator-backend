package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionRequest {
    private String englishText;
    private String tamilTranslation;
}
