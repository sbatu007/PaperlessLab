package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.errors.NotFoundException;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.repository.DocumentRepository;
import com.paperlesslab.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceUpdateDescriptionTest {

    @Mock
    DocumentRepository repository;

    @Mock
    RabbitMqProducer rabbitMqProducer; // wird hier nicht gebraucht, aber im Service-Konstruktor

    @InjectMocks
    DocumentService service;

    @Test
    void updateDescription_updatesAndSaves() {
        var doc = new Document();
        doc.setId(1L);
        doc.setFilename("test.pdf");
        doc.setDescription("old");

        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        when(repository.save(any(Document.class)))
                .thenAnswer(inv -> inv.getArgument(0, Document.class));

        var result = service.updateDescription(1L, "new description");

        assertThat(result.getDescription()).isEqualTo("new description");
        verify(repository).save(doc);
    }

    @Test
    void updateDescription_notFound_throwsNotFoundException() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDescription(42L, "whatever"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    void updateDescription_tooLong_throwsIllegalArgumentException() {
        // 2001 Zeichen, also > 2000
        String tooLong = "A".repeat(2001);

        assertThatThrownBy(() -> service.updateDescription(1L, tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum length");
    }
}
