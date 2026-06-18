package com.harekrishna.translator.config;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDbConfig {

    @Value("${llm.api.key}")
    private String openAiApiKey;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    @Bean
    public EmbeddingStore<dev.langchain4j.data.segment.TextSegment> pgVectorEmbeddingStore() {
        String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
        String[] parts = cleanUrl.split("/");
        String hostPort = parts[0];
        String database = parts[1].split("\\?")[0];
        
        String[] hp = hostPort.split(":");
        String host = hp[0];
        int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 5432;

        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(dbUser)
                .password(dbPassword)
                .table("translation_memory")
                .dimension(1536)
                .build();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingStore<dev.langchain4j.data.segment.TextSegment> store,
            EmbeddingModel embeddingModel) {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50)) // Smaller chunks for precise matching
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<dev.langchain4j.data.segment.TextSegment> store,
            EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();
    }
}
