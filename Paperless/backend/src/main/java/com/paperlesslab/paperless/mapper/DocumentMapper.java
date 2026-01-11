package com.paperlesslab.paperless.mapper;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.dto.LabelDto;
import com.paperlesslab.paperless.entity.Document;

import java.util.List;

public class DocumentMapper {

    public static DocumentDto toDto(Document document) {
        List<LabelDto> labels = document.getLabels() == null ? List.of() :
                document.getLabels().stream()
                        .map(l -> new LabelDto(l.getId(), l.getName()))
                        .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                        .toList();

        return new DocumentDto(
                document.getId(),
                document.getFilename(),
                document.getDescription(),
                document.getOcrText(),
                document.getResult(),
                labels
        );
    }

    public static Document toEntity(DocumentDto dto) {
        var e = new Document();
        if (dto.id() != null) e.setId(dto.id());
        e.setFilename(dto.filename());
        e.setDescription(dto.description());
        if (dto.ocrText() != null) e.setOcrText(dto.ocrText());
        if (dto.result() != null) e.setResult(dto.result());
        return e;
    }
}
