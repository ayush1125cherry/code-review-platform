package com.example.codereview.repository;

import com.example.codereview.auth.UserPrincipal;
import com.example.codereview.repository.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryImportService repositoryImportService;
    private final RepositoryFileService repositoryFileService;

    @PostMapping("/import")
    public ResponseEntity<RepositorySummaryDto> importRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ImportRepoRequest request) {
        return ResponseEntity.ok(repositoryImportService.importAndReview(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<RepositorySummaryDto>> getRepositories(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(repositoryImportService.getUserRepositories(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositorySummaryDto> getRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(repositoryImportService.getRepository(id, principal.getId()));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<RepoStatusResponse> getRepositoryStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(repositoryImportService.getStatus(id, principal.getId()));
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<RepositoryFileDto>> getRepositoryFiles(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        // Validate repo ownership
        repositoryImportService.getRepository(id, principal.getId());
        return ResponseEntity.ok(repositoryFileService.getFilesByRepository(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<RepositoryFileDto> getRepositoryFile(
            @PathVariable Long id,
            @RequestParam String path,
            @AuthenticationPrincipal UserPrincipal principal) {
        // Validate repo ownership
        repositoryImportService.getRepository(id, principal.getId());
        return ResponseEntity.ok(repositoryFileService.getFileByPath(id, path));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        repositoryImportService.deleteRepository(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
