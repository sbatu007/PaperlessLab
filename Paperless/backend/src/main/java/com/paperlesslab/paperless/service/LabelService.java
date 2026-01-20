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
    public Label rename(Long id, String name) {
        Label l = labelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Label %d not found".formatted(id)));

        String n = name == null ? "" : name.trim();
        if (n.isBlank()) throw new IllegalArgumentException("Label name is empty");

        // wenn es ein anderes Label mit dem Namen gibt -> dieses verwenden/ablehnen (deine Wahl)
        labelRepository.findByNameIgnoreCase(n).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Label name already exists");
            }
        });

        l.setName(n);
        return l;
    }

    public void delete(Long id) {
        Label l = labelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Label %d not found".formatted(id)));

        // wichtig: Beziehung lösen (Join-Table), sonst FK-Probleme möglich
        l.getDocuments().forEach(d -> d.getLabels().remove(l));
        l.getDocuments().clear();

        labelRepository.delete(l);
    }

}
