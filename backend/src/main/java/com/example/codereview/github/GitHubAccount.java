package com.example.codereview.github;

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
@Table(name = "github_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "github_user_id")
    private Long githubUserId;

    @Column(name = "github_username", nullable = false)
    private String githubUsername;

    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "profile_url")
    private String profileUrl;

    @CreationTimestamp
    @Column(name = "connected_at", updatable = false)
    private LocalDateTime connectedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
