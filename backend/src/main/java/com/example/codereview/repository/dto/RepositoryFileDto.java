package com.example.codereview.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryFileDto {
    private Long id;
    private Long repositoryId;
    private String filePath;
    private String fileName;
    private String fileExtension;
    private String language;
    private int lineCount;
    private long byteSize;
    private String content;
    private boolean isBinary;
}
