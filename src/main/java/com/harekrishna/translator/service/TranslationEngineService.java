package com.harekrishna.translator.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranslationEngineService {

    private IskconTranslator translator;

    @Value("${llm.api.key}")
    private String openAiApiKey;

    @Value("${llm.model}")
    private String modelName;

    @Value("${llm.base-url}")
    private String baseUrl;

    private final EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public TranslationEngineService(
            EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @PostConstruct
    public void init() {
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .build();

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(10)
                .minScore(0.7)
                .build();

        this.translator = AiServices.builder(IskconTranslator.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    public String translateText(String sourceText) {
        return translator.translate(sourceText);
    }

    interface IskconTranslator {
        @SystemMessage({
                "You are an expert translator specializing in Vaishnava literature (Bhagavad Gita, Ramayana, Puranas) from English to Tamil.",
                "CRITICAL INSTRUCTION: You will be provided with reference texts, vocabulary, and transliterations retrieved from a database of previously approved ISKCON translations.",
                "Use these references strictly as a 'Style and Vocabulary Guide' to understand the spiritual mood (Bhāva) and precise Vaishnava terminology (e.g., using 'முழுமுதற் கடவுள்' instead of generic words).",
                "HOWEVER, do NOT blindly copy the grammar of the reference sentences if the context is different. Use your own universal Tamil grammar engine to construct a natural-sounding, contextually appropriate Tamil sentence based on the story or verse you are currently translating.",
                "Ensure the final output is respectful, spiritually uplifting, and grammatically flawless. Only output the plain translated Tamil text."
        })
        @UserMessage("Translate the following text into Tamil, using the provided ISKCON references for vocabulary and mood:\n\n{{it}}")
        String translate(String sourceText);
    }
}
