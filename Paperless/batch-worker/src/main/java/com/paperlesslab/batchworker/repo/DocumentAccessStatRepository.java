package com.paperlesslab.batchworker.repo;

import com.paperlesslab.batchworker.entity.DocumentAccessStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DocumentAccessStatRepository extends JpaRepository<DocumentAccessStat, Long> {
    Optional<DocumentAccessStat> findByDocumentIdAndDay(Long documentId, LocalDate day);
}
