package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.search.IndexedDocumentView;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.multiMatch;

@Service
public class DocumentSearchService {

    private final ElasticsearchOperations operations;

    public DocumentSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public SearchHits<IndexedDocumentView> search(String q) {
        var query = NativeQuery.builder()
                .withQuery(multiMatch(m -> m
                        .query(q)
                        .fields("ocrText", "result", "filename")
                        .fuzziness("AUTO")
                ))
                .build();

        return operations.search(query, IndexedDocumentView.class);
    }
}
