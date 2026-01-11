package com.paperlesslab.paperless.repository;

import com.paperlesslab.paperless.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByNameIgnoreCase(String name);
}
