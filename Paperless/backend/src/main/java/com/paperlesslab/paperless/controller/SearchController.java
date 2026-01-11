package com.paperlesslab.paperless.controller;

import com.paperlesslab.paperless.service.DocumentSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final DocumentSearchService searchService;

    public SearchController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    public record SearchHitDto(Long documentId, String filename, float score) {}

    @GetMapping("/search")
    public List<SearchHitDto> search(@RequestParam("q") String q) {
        final String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return List.of();
        }
        try {
            var hits = searchService.search(query);
            if (hits == null || hits.getSearchHits() == null) {
                return List.of();
            }
            return hits.getSearchHits().stream()
                    .filter(h -> h != null && h.getContent() != null)
                    .map(h -> new SearchHitDto(
                            h.getContent().getDocumentId(),
                            h.getContent().getFilename(),
                            h.getScore()
                    ))
                    .toList();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Search failed for q='{}'", query, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ein unerwarteter Fehler ist aufgetreten.",
                    ex
            );
        }
    }
}
