package com.example.codereview.review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_findings", indexes = {
        @Index(name = "idx_findings_review_id", columnList = "review_id"),
        @Index(name = "idx_findings_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "references_json", columnDefinition = "TEXT")
    private String referencesJson; // JSON array of CodeReferenceDto
}
