package com.example.codereview;

import com.example.codereview.repository.RepositoryFileService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeReviewApplicationTests {

    @Test
    void testFileFiltering() {
        RepositoryFileService fileService = new RepositoryFileService(null);
        assertTrue(fileService.shouldIgnorePath(".git/config"));
        assertTrue(fileService.shouldIgnorePath("node_modules/react/index.js"));
        assertTrue(fileService.shouldIgnorePath("target/classes/App.class"));
        assertTrue(fileService.shouldIgnorePath("build/libs/app.jar"));
        assertTrue(fileService.shouldIgnorePath("dist/bundle.js"));
        assertTrue(fileService.shouldIgnorePath("image.png"));
        assertTrue(fileService.shouldIgnorePath(".env"));

        assertFalse(fileService.shouldIgnorePath("src/main/java/com/example/App.java"));
        assertFalse(fileService.shouldIgnorePath("package.json"));
        assertFalse(fileService.shouldIgnorePath("README.md"));
        assertFalse(fileService.shouldIgnorePath("pom.xml"));
    }

    @Test
    void testLanguageDetection() {
        RepositoryFileService fileService = new RepositoryFileService(null);
        assertEquals("Java", fileService.detectLanguage("JwtFilter.java"));
        assertEquals("TypeScript", fileService.detectLanguage("App.tsx"));
        assertEquals("Python", fileService.detectLanguage("main.py"));
        assertEquals("JSON", fileService.detectLanguage("package.json"));
        assertEquals("Markdown", fileService.detectLanguage("README.md"));
    }
}
