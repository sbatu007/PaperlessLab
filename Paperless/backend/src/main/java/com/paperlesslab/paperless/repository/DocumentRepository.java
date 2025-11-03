package com.paperlesslab.paperless.repository;

import com.paperlesslab.paperless.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
