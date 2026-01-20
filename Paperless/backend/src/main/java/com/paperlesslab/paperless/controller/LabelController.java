package com.paperlesslab.paperless.controller;

import com.paperlesslab.paperless.dto.LabelDto;
import com.paperlesslab.paperless.entity.Label;
import com.paperlesslab.paperless.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public List<LabelDto> list() {
        return labelService.list().stream()
                .map(l -> new LabelDto(l.getId(), l.getName()))
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<LabelDto> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        Label created = labelService.create(name);
        var dto = new LabelDto(created.getId(), created.getName());
        return ResponseEntity.created(URI.create("/labels/" + dto.id())).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public LabelDto rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        Label updated = labelService.rename(id, name);
        return new LabelDto(updated.getId(), updated.getName());
    }

}
