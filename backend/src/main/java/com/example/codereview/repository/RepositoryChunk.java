package com.example.codereview.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repository_chunks", indexes = {
        @Index(name = "idx_repo_chunks_repo_id", columnList = "repository_id"),
        @Index(name = "idx_repo_chunks_file_id", columnList = "file_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private RepositoryEntity repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private RepositoryFile file;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    private String language;

    @Column(name = "chunk_index")
    private int chunkIndex;

    @Column(name = "start_line")
    private int startLine;

    @Column(name = "end_line")
    private int endLine;

    @Column(name = "symbol_name")
    private String symbolName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String embeddingJson; // serialized float[]
}
