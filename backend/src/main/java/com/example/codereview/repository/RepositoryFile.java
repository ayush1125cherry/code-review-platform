package com.example.codereview.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repository_files", indexes = {
        @Index(name = "idx_repo_files_repo_id", columnList = "repository_id"),
        @Index(name = "idx_repo_files_path", columnList = "repository_id, file_path")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private RepositoryEntity repository;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_extension")
    private String fileExtension;

    private String language;

    @Column(name = "line_count")
    private int lineCount;

    @Column(name = "byte_size")
    private long byteSize;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_binary")
    private boolean isBinary;

    private String sha;
}
