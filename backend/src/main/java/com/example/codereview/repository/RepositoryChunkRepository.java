package com.example.codereview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RepositoryChunkRepository extends JpaRepository<RepositoryChunk, Long> {
    List<RepositoryChunk> findByRepositoryId(Long repositoryId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RepositoryChunk c WHERE c.repository.id = :repositoryId")
    void deleteByRepositoryId(@Param("repositoryId") Long repositoryId);

    long countByRepositoryId(Long repositoryId);
}
