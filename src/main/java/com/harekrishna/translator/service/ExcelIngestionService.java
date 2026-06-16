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

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelIngestionService {

    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    public void ingestExcel(InputStream inputStream, IngestionType type) {
        log.info("Starting ingestion for type: {}", type);
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

                        documents.add(new Document(englishText, metadata));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        log.info("Parsed {} rows. Starting ingestion into Vector DB...", documents.size());
        if (!documents.isEmpty()) {
            embeddingStoreIngestor.ingest(documents);
        }
        log.info("Ingestion complete.");
    }

    private String getCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }

    public void saveFeedback(String englishText, String tamilText, IngestionType type) {
        if (englishText == null || tamilText == null || englishText.isBlank() || tamilText.isBlank()) {
            throw new IllegalArgumentException("English and Tamil text cannot be empty");
        }
        
        Metadata metadata = new Metadata();
        metadata.put("tamil_output", tamilText);
        metadata.put("type", type.name());

        Document doc = new Document(englishText, metadata);
        embeddingStoreIngestor.ingest(List.of(doc));
        log.info("Successfully ingested feedback for type {}: {} -> {}", type, englishText, tamilText);
    }
}
