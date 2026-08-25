package com.example.codereview.repository;

import com.example.codereview.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, Long> {
    List<RepositoryEntity> findByUserOrderByUpdatedAtDesc(User user);
    List<RepositoryEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<RepositoryEntity> findByIdAndUserId(Long id, Long userId);
    Optional<RepositoryEntity> findFirstByIdAndUserId(Long id, Long userId);
    Optional<RepositoryEntity> findByUserIdAndFullName(Long userId, String fullName);
    Optional<RepositoryEntity> findFirstByUserIdAndFullNameOrderByIdDesc(Long userId, String fullName);
    Optional<RepositoryEntity> findByUserIdAndGithubRepoId(Long userId, Long githubRepoId);
    Optional<RepositoryEntity> findFirstByUserIdAndGithubRepoIdOrderByIdDesc(Long userId, Long githubRepoId);
    long countByUserId(Long userId);
}
