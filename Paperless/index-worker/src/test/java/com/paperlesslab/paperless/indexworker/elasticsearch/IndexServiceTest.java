package com.paperlesslab.paperless.indexworker.elasticsearch;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndexServiceTest {

    @Test
    void createsIndexIfMissing_andSaves() {
        ElasticsearchOperations ops = mock(ElasticsearchOperations.class);
        DocumentIndexRepository repo = mock(DocumentIndexRepository.class);
        IndexOperations indexOps = mock(IndexOperations.class);

        when(ops.indexOps(DocumentIndex.class)).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(false);

        IndexService service = new IndexService(ops, repo);
        service.index(1L, "HelloWorld.pdf", "Hello", "Summary");

        verify(indexOps).create();
        verify(indexOps).createMapping();
        verify(repo).save(any(DocumentIndex.class));
    }

    @Test
    void doesNotCreateIndexIfExists_butSaves() {
        ElasticsearchOperations ops = mock(ElasticsearchOperations.class);
        DocumentIndexRepository repo = mock(DocumentIndexRepository.class);
        IndexOperations indexOps = mock(IndexOperations.class);

        when(ops.indexOps(DocumentIndex.class)).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(true);

        IndexService service = new IndexService(ops, repo);
        service.index(2L, "a.pdf", "text", null);

        verify(indexOps, never()).create();
        verify(indexOps, never()).createMapping();  // Auch hier
        verify(repo).save(any(DocumentIndex.class));
    }
}
