package com.harekrishna.translator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadStatus {
    private String jobId;
    private int totalRows;
    private int processedRows;
    private String status; // "IN_PROGRESS", "COMPLETED", "FAILED"
    private String errorMessage;
}
