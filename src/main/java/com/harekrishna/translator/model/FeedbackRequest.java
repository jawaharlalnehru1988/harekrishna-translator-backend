package com.harekrishna.translator.model;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String englishText;
    private String correctedTamilText;
    private IngestionType type;
}
