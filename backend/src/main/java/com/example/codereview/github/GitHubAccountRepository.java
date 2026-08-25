package com.example.codereview.github;

import com.example.codereview.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitHubAccountRepository extends JpaRepository<GitHubAccount, Long> {
    Optional<GitHubAccount> findByUser(User user);
    Optional<GitHubAccount> findFirstByUserOrderByIdDesc(User user);
    Optional<GitHubAccount> findByUserId(Long userId);
    Optional<GitHubAccount> findFirstByUserIdOrderByIdDesc(Long userId);
    void deleteByUserId(Long userId);
}
