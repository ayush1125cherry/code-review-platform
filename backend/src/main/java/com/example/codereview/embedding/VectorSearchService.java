package com.example.codereview.embedding;

import com.example.codereview.repository.RepositoryChunk;
import com.example.codereview.repository.RepositoryChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final RepositoryChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public static class ScoredChunk {
        private final RepositoryChunk chunk;
        private final double score;

        public ScoredChunk(RepositoryChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public RepositoryChunk getChunk() {
            return chunk;
        }

        public double getScore() {
            return score;
        }
    }

    public List<RepositoryChunk> searchTopK(Long repositoryId, String query, int topK, String customApiKey) {
        List<RepositoryChunk> chunks = chunkRepository.findByRepositoryId(repositoryId);
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Float> queryVector = embeddingService.getEmbedding(query, customApiKey);
        if (queryVector.isEmpty()) {
            return chunks.subList(0, Math.min(topK, chunks.size()));
        }

        List<ScoredChunk> scoredList = new ArrayList<>();
        for (RepositoryChunk chunk : chunks) {
            List<Float> chunkVec = embeddingService.deserializeEmbedding(chunk.getEmbeddingJson());
            double sim = cosineSimilarity(queryVector, chunkVec);
            // Boost exact keyword occurrences in chunk
            String queryLower = query.toLowerCase();
            String contentLower = chunk.getContent().toLowerCase();
            if (contentLower.contains(queryLower)) {
                sim += 0.2;
            }
            if (chunk.getFilePath().toLowerCase().contains(queryLower)) {
                sim += 0.3;
            }
            scoredList.add(new ScoredChunk(chunk, sim));
        }

        scoredList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<RepositoryChunk> results = new ArrayList<>();
        int limit = Math.min(topK, scoredList.size());
        for (int i = 0; i < limit; i++) {
            results.add(scoredList.get(i).getChunk());
        }
        return results;
    }

    private double cosineSimilarity(List<Float> vecA, List<Float> vecB) {
        if (vecA == null || vecB == null || vecA.isEmpty() || vecB.isEmpty() || vecA.size() != vecB.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.size(); i++) {
            double a = vecA.get(i);
            double b = vecB.get(i);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA <= 0.0 || normB <= 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
