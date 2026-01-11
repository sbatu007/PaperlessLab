package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.entity.Label;
import com.paperlesslab.paperless.errors.NotFoundException;
import com.paperlesslab.paperless.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    @Transactional(readOnly = true)
    public List<Label> list() {
        return labelRepository.findAll();
    }

    public Label create(String name) {
        String n = name == null ? "" : name.trim();
        if (n.isBlank()) throw new IllegalArgumentException("Label name is empty");

        return labelRepository.findByNameIgnoreCase(n)
                .orElseGet(() -> labelRepository.save(new Label(n)));
    }

    public void delete(Long id) {
        Label l = labelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Label %d not found".formatted(id)));
        labelRepository.delete(l);
    }
}
