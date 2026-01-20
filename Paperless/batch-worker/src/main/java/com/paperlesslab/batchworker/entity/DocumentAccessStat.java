package com.paperlesslab.batchworker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "document_access_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "day"})
)
public class DocumentAccessStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="document_id", nullable=false)
    private Long documentId;

    @Column(name="day", nullable=false)
    private LocalDate day;

    @Column(name="access_count", nullable=false)
    private long accessCount;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    public DocumentAccessStat() {}

    public DocumentAccessStat(Long documentId, LocalDate day, long accessCount) {
        this.documentId = documentId;
        this.day = day;
        this.accessCount = accessCount;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
    public long getAccessCount() { return accessCount; }
    public void setAccessCount(long accessCount) { this.accessCount = accessCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
