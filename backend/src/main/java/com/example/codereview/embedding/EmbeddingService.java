package com.example.codereview.embedding;

import com.example.codereview.ai.GeminiApiClient;
import com.example.codereview.repository.RepositoryChunk;
import com.example.codereview.repository.RepositoryChunkRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final GeminiApiClient geminiApiClient;
    private final RepositoryChunkRepository chunkRepository;
    private final ObjectMapper objectMapper;

    public List<Float> getEmbedding(String text, String customApiKey) {
        return geminiApiClient.generateEmbedding(text, customApiKey);
    }

    public String serializeEmbedding(List<Float> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            log.error("Failed to serialize embedding", e);
            return "[]";
        }
    }

    public List<Float> deserializeEmbedding(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize embedding JSON", e);
            return new ArrayList<>();
        }
    }
}
