package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.documents.DocumentDto;
import com.paperlesslab.paperless.errors.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> list() {
        return repository.findAll().stream().map(DocumentMapper::toDto).toList();
    }

    public DocumentDto create(DocumentDto dto) {
        Document entity = DocumentMapper.toEntity(dto);
        var saved = repository.save(entity);
        return DocumentMapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public DocumentDto get(Long id) {
        var e = repository.findById(id).orElseThrow(() -> new NotFoundException("Document %d not found".formatted(id)));
        return DocumentMapper.toDto(e);
    }


    public void delete(Long id) {
        if (!repository.existsById(id)) throw new NotFoundException("Document %d not found".formatted(id));
        repository.deleteById(id);
    }

}
