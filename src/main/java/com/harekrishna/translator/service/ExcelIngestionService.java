package com.harekrishna.translator.service;

import com.harekrishna.translator.model.IngestionType;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.harekrishna.translator.model.UploadStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelIngestionService {

    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    private final Map<String, UploadStatus> uploadStatuses = new ConcurrentHashMap<>();

    public UploadStatus getUploadStatus(String jobId) {
        return uploadStatuses.get(jobId);
    }

    public String ingestExcel(InputStream inputStream, IngestionType type, String scriptureContext) {
        log.info("Starting ingestion for type: {}, scripture: {}", type, scriptureContext);
        List<Document> documents = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Cell englishCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell tamilCell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (englishCell != null && tamilCell != null) {
                    String englishText = getCellValue(englishCell);
                    String tamilText = getCellValue(tamilCell);

                    if (!englishText.isEmpty() && !tamilText.isEmpty()) {
                        Metadata metadata = new Metadata();
                        metadata.put("tamil_output", tamilText);
                        metadata.put("type", type.name());
                        metadata.put("scripture_context", scriptureContext);

                        documents.add(new Document(englishText, metadata));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        log.info("Parsed {} rows. Starting ingestion into Vector DB in batches...", documents.size());
        
        String jobId = UUID.randomUUID().toString();
        UploadStatus status = new UploadStatus(jobId, documents.size(), 0, "IN_PROGRESS", null);
        uploadStatuses.put(jobId, status);

        if (!documents.isEmpty()) {
            new Thread(() -> {
                try {
                    int batchSize = 50; // Reduced to 50 because Purports are very long and split into many chunks
                    for (int i = 0; i < documents.size(); i += batchSize) {
                        int end = Math.min(documents.size(), i + batchSize);
                        List<Document> batch = documents.subList(i, end);
                        log.info("Ingesting batch {} to {}...", i, end);
                        embeddingStoreIngestor.ingest(batch);
                        
                        status.setProcessedRows(end);
                    }
                    log.info("Ingestion complete for type: {}", type);
                    status.setStatus("COMPLETED");
                } catch (Exception e) {
                    log.error("Background ingestion failed", e);
                    status.setStatus("FAILED");
                    status.setErrorMessage(e.getMessage());
                }
            }).start();
        } else {
            status.setStatus("COMPLETED");
        }
        
        return jobId;
    }

    private String getCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }

    public void saveFeedback(String englishText, String tamilText, IngestionType type, String scriptureContext) {
        if (englishText == null || tamilText == null || englishText.isBlank() || tamilText.isBlank()) {
            throw new IllegalArgumentException("English and Tamil text cannot be empty");
        }
        
        Metadata metadata = new Metadata();
        metadata.put("tamil_output", tamilText);
        metadata.put("type", type.name());
        metadata.put("scripture_context", scriptureContext);

        Document doc = new Document(englishText, metadata);
        embeddingStoreIngestor.ingest(List.of(doc));
        log.info("Successfully ingested feedback for type {}: {} -> {}", type, englishText, tamilText);
    }
}
