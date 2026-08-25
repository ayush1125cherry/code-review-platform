package com.example.codereview.repository;

import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.repository.dto.RepositoryFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoryFileService {

    private final RepositoryFileRepository fileRepository;

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", "node_modules", "target", "build", "dist", "out", ".idea", ".vscode",
            ".mvn", "vendor", "__pycache__", ".gradle", "bin", "obj", ".next", ".nuxt", "coverage"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "ico", "svg", "webp", "mp4", "mp3", "pdf",
            "zip", "tar", "gz", "7z", "jar", "war", "ear", "class", "exe", "dll", "so", "dylib",
            "lock", "min.js", "min.css", "map", "ttf", "woff", "woff2", "eot", "env"
    );

    public boolean shouldIgnorePath(String path) {
        if (path == null || path.isBlank()) return true;
        String normalized = path.replace('\\', '/').toLowerCase();

        // Check ignored directories
        String[] parts = normalized.split("/");
        for (String part : parts) {
            if (IGNORED_DIRS.contains(part) || part.startsWith(".")) {
                if (!part.equals(".github") && !part.equals(".gitignore")) {
                    return true;
                }
            }
        }

        // Check extensions
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < normalized.length() - 1) {
            String ext = normalized.substring(dotIndex + 1);
            if (IGNORED_EXTENSIONS.contains(ext)) {
                return true;
            }
        }

        return false;
    }

    public String detectLanguage(String fileName) {
        if (fileName == null) return "Text";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return "Text";
        String ext = fileName.substring(dotIndex + 1).toLowerCase();

        return switch (ext) {
            case "java" -> "Java";
            case "js", "mjs", "cjs" -> "JavaScript";
            case "ts", "tsx" -> "TypeScript";
            case "jsx" -> "React JSX";
            case "py" -> "Python";
            case "go" -> "Go";
            case "rs" -> "Rust";
            case "c", "h" -> "C";
            case "cpp", "hpp", "cc" -> "C++";
            case "cs" -> "C#";
            case "kt", "kts" -> "Kotlin";
            case "rb" -> "Ruby";
            case "php" -> "PHP";
            case "swift" -> "Swift";
            case "sql" -> "SQL";
            case "html", "htm" -> "HTML";
            case "css", "scss", "sass", "less" -> "CSS";
            case "json" -> "JSON";
            case "xml" -> "XML";
            case "yaml", "yml" -> "YAML";
            case "md", "markdown" -> "Markdown";
            case "sh", "bash" -> "Shell";
            case "dockerfile" -> "Dockerfile";
            default -> ext.toUpperCase();
        };
    }

    @Transactional(readOnly = true)
    public List<RepositoryFileDto> getFilesByRepository(Long repositoryId) {
        return fileRepository.findByRepositoryId(repositoryId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepositoryFileDto getFileByPath(Long repositoryId, String filePath) {
        RepositoryFile file = fileRepository.findByRepositoryIdAndFilePath(repositoryId, filePath)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filePath));
        return mapToDto(file);
    }

    public RepositoryFileDto mapToDto(RepositoryFile file) {
        return RepositoryFileDto.builder()
                .id(file.getId())
                .repositoryId(file.getRepository().getId())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .fileExtension(file.getFileExtension())
                .language(file.getLanguage())
                .lineCount(file.getLineCount())
                .byteSize(file.getByteSize())
                .content(file.getContent())
                .isBinary(file.isBinary())
                .build();
    }
}
