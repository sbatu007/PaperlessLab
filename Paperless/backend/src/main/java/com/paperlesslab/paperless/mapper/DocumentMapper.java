package com.paperlesslab.paperless.mapper;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.entity.Document;

public class DocumentMapper {

    public static DocumentDto toDto(Document document) {
        return new DocumentDto(document.getId(), document.getFilename(), document.getDescription());
    }
    public static Document toEntity(DocumentDto dto) {
        var e = new Document();
        e.setId(dto.id());
        e.setFilename(dto.filename());
        e.setDescription(dto.description());
        return e;
    }
}
