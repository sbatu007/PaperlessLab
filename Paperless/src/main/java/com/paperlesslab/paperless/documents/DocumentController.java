package com.paperlesslab.paperless.documents;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.paperlesslab.paperless.documents.DocumentDto;

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
        return documentService.list();
    }

    @PostMapping
    public ResponseEntity<DocumentDto> create(@Valid @RequestBody DocumentDto dto) {
        var saved = documentService.create(dto);
        return ResponseEntity.created(URI.create("/documents/" + saved.id())).body(saved);
    }

    @GetMapping("/{id}")
    public DocumentDto get(@PathVariable Long id) {
        return documentService.get(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "description", required = false) String description) throws IOException {
        var original = StringUtils.cleanPath(file.getOriginalFilename());
        if (original.contains("..")) throw new IllegalArgumentException("Invalid filename");
        var target = uploadDir.resolve(original);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);


        var doc = new Document();
        doc.setFilename(original);
        doc.setDescription(description);
        var saved = documentService.create(new DocumentDto(null, doc.getFilename(), doc.getDescription()));
        return ResponseEntity.created(URI.create("/documents/" + saved.id())).body(saved);
    }

}
