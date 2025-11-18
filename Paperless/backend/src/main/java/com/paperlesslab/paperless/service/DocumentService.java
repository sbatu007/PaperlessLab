package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.errors.NotFoundException;
import com.paperlesslab.paperless.mapper.DocumentMapper;
import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;
    private final RabbitMqProducer rabbitMqProducer;

    public DocumentService(DocumentRepository repository,  RabbitMqProducer rabbitMqProducer) {
        this.repository = repository;
        this.rabbitMqProducer = rabbitMqProducer;
    }


    @Transactional(readOnly = true)
    public List<Document> list() {
        return repository.findAll();
    }

    public Document create(Document entity) {
        Document saved = repository.save(entity);

        DocumentUploadMessage message =
                new DocumentUploadMessage(saved.getId(), saved.getFilename());
        rabbitMqProducer.sendDocumentUploaded(message);

        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Document get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document %d not found".formatted(id)));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Document %d not found".formatted(id));
        }
        repository.deleteById(id);
    }

}
