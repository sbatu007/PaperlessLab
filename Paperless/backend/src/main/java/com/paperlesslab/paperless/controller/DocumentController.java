package com.paperlesslab.paperless.controller;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.mapper.DocumentMapper;
import com.paperlesslab.paperless.service.DocumentService;
import com.paperlesslab.paperless.entity.Document;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final Path uploadDir = Paths.get("uploads");

    public DocumentController(DocumentService documentService) throws IOException {
        this.documentService = documentService;
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    @GetMapping
    public List<DocumentDto> list() {
        return documentService.list().stream()
                .map(DocumentMapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<DocumentDto> create(@RequestBody DocumentDto dto) {
        Document entity = DocumentMapper.toEntity(dto);
        Document saved = documentService.create(entity);
        DocumentDto out = DocumentMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/documents/" + out.id()))
                .body(out);
    }

    @GetMapping("/{id}")
    public DocumentDto get(@PathVariable Long id) {
        Document entity = documentService.get(id);
        return DocumentMapper.toDto(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "description", required = false) String description)
            throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        if (original.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Path target = uploadDir.resolve(original);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Document doc = new Document();
        doc.setFilename(original);
        doc.setDescription(description);

        Document saved = documentService.create(doc);
        DocumentDto out = DocumentMapper.toDto(saved);

        return ResponseEntity.created(URI.create("/documents/" + out.id()))
                .body(out);
    }

}
