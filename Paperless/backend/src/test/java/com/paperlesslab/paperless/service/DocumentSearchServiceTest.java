package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.search.IndexedDocumentView;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;

import static org.mockito.Mockito.*;

class DocumentSearchServiceTest {

    @Test
    void callsElasticsearchOperationsSearch() {
        ElasticsearchOperations ops = mock(ElasticsearchOperations.class);
        DocumentSearchService svc = new DocumentSearchService(ops);

        svc.search("Hello");

        verify(ops).search(any(Query.class), eq(IndexedDocumentView.class));
    }
}
