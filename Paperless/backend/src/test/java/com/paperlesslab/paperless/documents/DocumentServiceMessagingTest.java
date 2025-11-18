package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.repository.DocumentRepository;
import com.paperlesslab.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceMessagingTest {

    @Mock
    DocumentRepository repository;

    @Mock
    RabbitMqProducer rabbitMqProducer;

    @InjectMocks
    DocumentService documentService;

    @Test
    void create_sendsMessageToQueue() {
        Document unsaved = new Document();
        unsaved.setFilename("test.pdf");
        unsaved.setDescription("demo");

        Document saved = new Document();
        saved.setId(1L);
        saved.setFilename("test.pdf");
        saved.setDescription("demo");

        when(repository.save(any(Document.class))).thenReturn(saved);

        Document result = documentService.create(unsaved);

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage> captor =
                ArgumentCaptor.forClass(com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage.class);

        verify(rabbitMqProducer, times(1)).sendDocumentUploaded(captor.capture());

        var message = captor.getValue();
        assertThat(message.documentId()).isEqualTo(1L);
        assertThat(message.filename()).isEqualTo("test.pdf");
        assertThat(message.description()).isEqualTo("demo");
    }
}
