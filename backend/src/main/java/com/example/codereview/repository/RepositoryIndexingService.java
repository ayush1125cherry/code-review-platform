package com.example.codereview.repository;

import com.example.codereview.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryIndexingService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIndexingService.class);

    private final RepositoryChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    private static final int CHUNK_SIZE_LINES = 60;
    private static final int CHUNK_OVERLAP_LINES = 10;

    @Transactional
    public List<RepositoryChunk> indexFiles(RepositoryEntity repository, List<RepositoryFile> files, String customApiKey) {
        chunkRepository.deleteByRepositoryId(repository.getId());
        List<RepositoryChunk> allChunks = new ArrayList<>();

        for (RepositoryFile file : files) {
            if (file.isBinary() || file.getContent() == null || file.getContent().isBlank()) {
                continue;
            }

            List<RepositoryChunk> fileChunks = createChunksForFile(repository, file, customApiKey);
            allChunks.addAll(fileChunks);
        }

        log.info("Saving {} indexed chunks for repository {}", allChunks.size(), repository.getFullName());
        return chunkRepository.saveAll(allChunks);
    }

    private List<RepositoryChunk> createChunksForFile(RepositoryEntity repository, RepositoryFile file, String customApiKey) {
        List<RepositoryChunk> chunks = new ArrayList<>();
        String[] lines = file.getContent().split("\\r?\\n");
        int totalLines = lines.length;

        if (totalLines <= CHUNK_SIZE_LINES) {
            // Single chunk for small file
            String content = String.join("\n", lines);
            List<Float> embedding = embeddingService.getEmbedding(content, customApiKey);
            String embeddingJson = embeddingService.serializeEmbedding(embedding);

            RepositoryChunk chunk = RepositoryChunk.builder()
                    .repository(repository)
                    .file(file)
                    .filePath(file.getFilePath())
                    .language(file.getLanguage())
                    .chunkIndex(0)
                    .startLine(1)
                    .endLine(totalLines)
                    .symbolName(file.getFileName())
                    .content(content)
                    .embeddingJson(embeddingJson)
                    .build();
            chunks.add(chunk);
            return chunks;
        }

        int chunkIndex = 0;
        int start = 0;

        while (start < totalLines) {
            int end = Math.min(start + CHUNK_SIZE_LINES, totalLines);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                sb.append(lines[i]).append("\n");
            }
            String content = sb.toString().trim();

            if (!content.isBlank()) {
                List<Float> embedding = embeddingService.getEmbedding(content, customApiKey);
                String embeddingJson = embeddingService.serializeEmbedding(embedding);

                RepositoryChunk chunk = RepositoryChunk.builder()
                        .repository(repository)
                        .file(file)
                        .filePath(file.getFilePath())
                        .language(file.getLanguage())
                        .chunkIndex(chunkIndex++)
                        .startLine(start + 1)
                        .endLine(end)
                        .symbolName(file.getFileName() + " [" + (start + 1) + "-" + end + "]")
                        .content(content)
                        .embeddingJson(embeddingJson)
                        .build();
                chunks.add(chunk);
            }

            if (end >= totalLines) {
                break;
            }
            start += (CHUNK_SIZE_LINES - CHUNK_OVERLAP_LINES);
        }

        return chunks;
    }
}
