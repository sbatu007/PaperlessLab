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
import java.util.Map;

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
    public ResponseEntity<DocumentDto> create(@Valid @RequestBody DocumentDto dto) {
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

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "upload.pdf"; // minimaler Fallback, keine neuen Variablen in der DB
        }

        String cleaned = StringUtils.cleanPath(original);
        if (cleaned.contains("..") || cleaned.startsWith("/") ) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Files.createDirectories(uploadDir);

        String safeName = cleaned;
        Path target = uploadDir.resolve(safeName);
        if (Files.exists(target)) {
            int dot = safeName.lastIndexOf('.');
            String name = (dot > 0) ? safeName.substring(0, dot) : safeName;
            String ext  = (dot > 0) ? safeName.substring(dot)      : "";
            safeName = name + "-" + System.currentTimeMillis() + ext;
            target = uploadDir.resolve(safeName);
        }

        Files.copy(file.getInputStream(), target);

        Document doc = new Document();
        doc.setFilename(safeName);
        doc.setDescription(description);

        Document saved = documentService.create(doc);
        DocumentDto out = DocumentMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/documents/" + out.id())).body(out);
    }
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String description = body.get("description");
        Document updated = documentService.updateDescription(id, description);
        return ResponseEntity.ok(DocumentMapper.toDto(updated));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String,String>> tooLarge(Exception ex) {
        return ResponseEntity.status(413).body(Map.of("error", "File too large"));
    }
}
