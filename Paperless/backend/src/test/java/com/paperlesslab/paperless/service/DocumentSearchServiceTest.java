package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.search.IndexedDocumentView;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.Query;

import static org.mockito.Mockito.*;

class DocumentSearchServiceTest {

    @Test
    void callsElasticsearchOperationsSearch() {
        ElasticsearchOperations ops = mock(ElasticsearchOperations.class);
        IndexOperations indexOps = mock(IndexOperations.class);

        when(ops.indexOps(IndexedDocumentView.class)).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(true);

        DocumentSearchService svc = new DocumentSearchService(ops);

        svc.search("Hello");

        verify(ops).search(any(Query.class), eq(IndexedDocumentView.class));
    }
}
