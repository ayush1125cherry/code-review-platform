package com.example.codereview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {
    List<RepositoryFile> findByRepositoryId(Long repositoryId);
    Optional<RepositoryFile> findByRepositoryIdAndFilePath(Long repositoryId, String filePath);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RepositoryFile f WHERE f.repository.id = :repositoryId")
    void deleteByRepositoryId(@Param("repositoryId") Long repositoryId);

    long countByRepositoryId(Long repositoryId);
}
