package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.errors.NotFoundException;
import com.paperlesslab.paperless.minio.FileStorageService;
import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repository;
    private final RabbitMqProducer rabbitMqProducer;
    private final FileStorageService fileStorageService;
    private final SearchIndexService searchIndexService;

    @Transactional(readOnly = true)
    public List<Document> list() {
        return repository.findAll();
    }

    public Document create(Document entity) {
        Document saved = repository.save(entity);

        DocumentUploadMessage message = new DocumentUploadMessage(
                saved.getId(),
                saved.getFilename(),
                saved.getDescription()
        );
        rabbitMqProducer.sendDocumentUploaded(message);

        return saved;
    }

    @Transactional(readOnly = true)
    public Document get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document %d not found".formatted(id)));
    }

    public void delete(Long id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document %d not found".formatted(id)));

        repository.delete(doc);

        try {
            Path uploads = Paths.get("uploads");
            Path p = uploads.resolve(doc.getFilename());
            Files.deleteIfExists(p);
        } catch (Exception e) {
            // soll nicht blockieren
        }

        fileStorageService.deletePdf(doc.getFilename());

        searchIndexService.deleteFromIndex(id);
    }

    public Document updateDescription(Long id, String description) {
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Description exceeds maximum length of 2000 characters");
        }

        var doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        doc.setDescription(description);
        return repository.save(doc);
    }

    public Document updateOcrAndSummary(Long id, String ocrText, String summary) {
        var doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document %d not found".formatted(id)));

        if (ocrText != null) doc.setOcrText(ocrText);
        if (summary != null) doc.setResult(summary);

        return repository.save(doc);
    }
}
