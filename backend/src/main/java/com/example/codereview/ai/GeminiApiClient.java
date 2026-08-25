package com.example.codereview.ai;

import com.example.codereview.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key:}")
    private String defaultApiKey;

    @Value("${app.gemini.model:gemini-1.5-flash}")
    private String defaultModel;

    @Value("${app.gemini.embedding-model:text-embedding-004}")
    private String defaultEmbeddingModel;

    @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public String generateContent(String prompt, String systemInstruction, boolean jsonMode, String customApiKey) {
        String apiKey = resolveApiKey(customApiKey);
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException("Gemini API key is not configured. Please set GEMINI_API_KEY in environment or configure it in Settings.", HttpStatus.BAD_REQUEST);
        }

        List<String> candidateModels = List.of(
                defaultModel,
                "gemini-2.0-flash",
                "gemini-1.5-flash-latest",
                "gemini-1.5-flash",
                "gemini-1.5-pro",
                "gemini-2.5-flash"
        );

        Exception lastException = null;

        for (String model : candidateModels) {
            try {
                ObjectNode root = objectMapper.createObjectNode();

                // System instruction
                if (systemInstruction != null && !systemInstruction.isBlank()) {
                    ObjectNode sysNode = root.putObject("system_instruction");
                    ObjectNode sysParts = sysNode.putArray("parts").addObject();
                    sysParts.put("text", systemInstruction);
                }

                // Contents
                ArrayNode contentsNode = root.putArray("contents");
                ObjectNode userContent = contentsNode.addObject();
                userContent.put("role", "user");
                ObjectNode userParts = userContent.putArray("parts").addObject();
                userParts.put("text", prompt);

                // Generation config
                ObjectNode genConfig = root.putObject("generationConfig");
                genConfig.put("temperature", 0.2);
                if (jsonMode) {
                    genConfig.put("responseMimeType", "application/json");
                }

                String url = String.format("%s/models/%s:generateContent", baseUrl, model);

                String response = WebClient.builder()
                        .build()
                        .post()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("x-goog-api-key", apiKey)
                        .bodyValue(root.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode responseJson = objectMapper.readTree(response);
                JsonNode candidates = responseJson.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode parts = firstCandidate.path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText();
                    }
                }
            } catch (WebClientResponseException e) {
                log.warn("Gemini model {} returned status {}: {}", model, e.getStatusCode(), e.getResponseBodyAsString());
                lastException = e;
                if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getResponseBodyAsString().contains("not found")) {
                    // Try next model candidate
                    continue;
                }
                String errorMsg = "Gemini API error: " + e.getStatusCode();
                try {
                    JsonNode errJson = objectMapper.readTree(e.getResponseBodyAsString());
                    if (errJson.has("error") && errJson.get("error").has("message")) {
                        errorMsg = errJson.get("error").get("message").asText();
                    }
                } catch (Exception ignored) {}
                throw new ApiException(errorMsg, HttpStatus.valueOf(e.getStatusCode().value()));
            } catch (Exception e) {
                log.warn("Gemini model {} failed with exception: {}", model, e.getMessage());
                lastException = e;
            }
        }

        if (lastException instanceof ApiException) throw (ApiException) lastException;
        throw new ApiException("Error communicating with Gemini AI: " + (lastException != null ? lastException.getMessage() : "All models unavailable"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public List<Float> generateEmbedding(String text, String customApiKey) {
        String apiKey = resolveApiKey(customApiKey);
        if (apiKey == null || apiKey.isBlank()) {
            return generateDeterministicFallbackEmbedding(text);
        }

        List<String> embeddingModels = List.of(defaultEmbeddingModel, "text-embedding-004", "embedding-001");

        for (String embModel : embeddingModels) {
            try {
                ObjectNode root = objectMapper.createObjectNode();
                ObjectNode content = root.putObject("content");
                ObjectNode part = content.putArray("parts").addObject();
                String cleanText = text.length() > 2000 ? text.substring(0, 2000) : text;
                part.put("text", cleanText);

                String url = String.format("%s/models/%s:embedContent", baseUrl, embModel);

                String response = WebClient.builder()
                        .build()
                        .post()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("x-goog-api-key", apiKey)
                        .bodyValue(root.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode responseJson = objectMapper.readTree(response);
                JsonNode values = responseJson.path("embedding").path("values");
                if (values.isArray() && values.size() > 0) {
                    List<Float> embedding = new ArrayList<>(values.size());
                    for (JsonNode v : values) {
                        embedding.add((float) v.asDouble());
                    }
                    return embedding;
                }
            } catch (Exception e) {
                log.warn("Gemini embedding model {} failed, trying next: {}", embModel, e.getMessage());
            }
        }

        return generateDeterministicFallbackEmbedding(text);
    }

    private List<Float> generateDeterministicFallbackEmbedding(String text) {
        int dim = 128;
        List<Float> vec = new ArrayList<>(dim);
        for (int i = 0; i < dim; i++) {
            vec.add(0.0f);
        }

        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = Math.abs(token.hashCode());
            int idx = hash % dim;
            vec.set(idx, vec.get(idx) + 1.0f);
        }

        // Normalize
        float norm = 0.0f;
        for (float v : vec) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0.0f) {
            for (int i = 0; i < dim; i++) {
                vec.set(i, vec.get(i) / norm);
            }
        }
        return vec;
    }

    private String resolveApiKey(String customApiKey) {
        if (customApiKey != null && !customApiKey.isBlank()) {
            return customApiKey;
        }
        return defaultApiKey;
    }
}
