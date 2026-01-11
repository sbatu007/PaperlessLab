package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.search.IndexedDocumentView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
public class SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexService.class);
    private final ElasticsearchOperations operations;

    public SearchIndexService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public void deleteFromIndex(Long documentId) {
        try {
            operations.delete(String.valueOf(documentId), IndexedDocumentView.class);
            log.info("Deleted documentId={} from Elasticsearch index", documentId);
        } catch (Exception e) {
            log.warn("Could not delete documentId={} from Elasticsearch (ignored)", documentId, e);
        }
    }
}
