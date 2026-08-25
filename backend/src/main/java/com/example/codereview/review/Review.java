package com.example.codereview.review;

import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_repo_id", columnList = "repository_id"),
        @Index(name = "idx_reviews_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "overall_score")
    private int overallScore; // 0 - 100

    @Column(name = "architecture_score")
    private int architectureScore;

    @Column(name = "code_quality_score")
    private int codeQualityScore;

    @Column(name = "efficiency_score")
    private int efficiencyScore;

    @Column(name = "security_score")
    private int securityScore;

    @Column(name = "maintainability_score")
    private int maintainabilityScore;

    @Column(name = "testing_score")
    private int testingScore;

    @Column(name = "documentation_score")
    private int documentationScore;

    @Column(name = "repository_type")
    private String repositoryType;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "technologies_json", columnDefinition = "TEXT")
    private String technologiesJson;

    @Column(name = "architecture_summary", columnDefinition = "TEXT")
    private String architectureSummary;

    @Column(name = "efficiency_summary", columnDefinition = "TEXT")
    private String efficiencySummary;

    @Column(name = "security_summary", columnDefinition = "TEXT")
    private String securitySummary;

    @Column(name = "maintainability_summary", columnDefinition = "TEXT")
    private String maintainabilitySummary;

    @Column(name = "testing_summary", columnDefinition = "TEXT")
    private String testingSummary;

    @Column(name = "documentation_summary", columnDefinition = "TEXT")
    private String documentationSummary;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewFinding> findings = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
