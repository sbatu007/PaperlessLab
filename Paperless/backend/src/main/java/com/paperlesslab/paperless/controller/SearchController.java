package com.paperlesslab.paperless.controller;

import com.paperlesslab.paperless.service.DocumentSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private final DocumentSearchService searchService;

    public SearchController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    public record SearchHitDto(Long documentId, String filename, float score) {}

    @GetMapping("/api/documents/search")
    public List<SearchHitDto> search(@RequestParam("q") String q) {
        var hits = searchService.search(q);

        return hits.getSearchHits().stream()
                .map(h -> new SearchHitDto(
                        h.getContent().getDocumentId(),
                        h.getContent().getFilename(),
                        h.getScore()
                ))
                .toList();
    }
}
