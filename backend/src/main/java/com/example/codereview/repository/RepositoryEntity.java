package com.example.codereview.repository;

import com.example.codereview.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "github_repo_id")
    private Long githubRepoId;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_branch")
    private String defaultBranch;

    private String language;

    @Column(name = "html_url")
    private String htmlUrl;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "stars_count")
    private int starsCount;

    @Column(name = "forks_count")
    private int forksCount;

    @Column(name = "size_kb")
    private long sizeKb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RepoStatus status = RepoStatus.PENDING;

    @Column(name = "status_step")
    private String statusStep; // e.g. "Importing files (42/100)..."

    @Column(name = "status_progress")
    private int statusProgress; // 0 - 100

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
