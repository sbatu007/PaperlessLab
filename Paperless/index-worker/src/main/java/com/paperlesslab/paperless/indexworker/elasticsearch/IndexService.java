package com.paperlesslab.paperless.indexworker.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexService.class);

    private final ElasticsearchOperations operations;
    private final DocumentIndexRepository repository;

    public IndexService(ElasticsearchOperations operations, DocumentIndexRepository repository) {
        this.operations = operations;
        this.repository = repository;
    }

    public void ensureIndexExists() {
        var indexOps = operations.indexOps(DocumentIndex.class);
        if (!indexOps.exists()) {
            log.info("Index 'documents' missing -> creating");
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
        }
    }

    public void index(Long documentId, String filename, String ocrText, String result) {
        ensureIndexExists();
        String id = String.valueOf(documentId);

        DocumentIndex doc = new DocumentIndex(
                id,
                documentId,
                filename,
                ocrText,
                result,
                Instant.now()
        );

        repository.save(doc);
    }
}
