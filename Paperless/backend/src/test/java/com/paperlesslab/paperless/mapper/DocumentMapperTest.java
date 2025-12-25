package com.paperlesslab.paperless.mapper;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = new DocumentMapper();

    @Test
    void toDto_mapsAllFields() {
        Document entity = new Document(
                1L,
                "test.pdf",
                "desc",
                LocalDateTime.now(),
                "OCR content",
                "Result content"
        );

        DocumentDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.filename()).isEqualTo("test.pdf");
        assertThat(dto.description()).isEqualTo("desc");
        assertThat(dto.ocrText()).isEqualTo("OCR content");
        assertThat(dto.result()).isEqualTo("Result content");
    }

    @Test
    void toEntity_mapsAllFields() {
        DocumentDto dto = new DocumentDto(2L, "file.pdf", "description", "OCR", "Result");

        Document entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getFilename()).isEqualTo("file.pdf");
        assertThat(entity.getDescription()).isEqualTo("description");
        assertThat(entity.getOcrText()).isEqualTo("OCR");
        assertThat(entity.getResult()).isEqualTo("Result");
    }

    @Test
    void toEntity_withNullOptionalFields_doesNotSetThem() {
        DocumentDto dto = new DocumentDto(null, "file.pdf", "description", null, null);

        Document entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getFilename()).isEqualTo("file.pdf");
        assertThat(entity.getDescription()).isEqualTo("description");
        assertThat(entity.getOcrText()).isNull();
        assertThat(entity.getResult()).isNull();
    }
}
