package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
class DocumentServiceMessagingTest {

    @Autowired
    DocumentService documentService;

    @MockBean
    RabbitMqProducer rabbitMqProducer;

    @Test
    void create_sendsMessageToOcrQueue() {
        var doc = new Document();
        doc.setFilename("test.pdf");
        doc.setDescription("test");

        documentService.create(doc);

        verify(rabbitMqProducer).sendDocumentUploaded(any(DocumentUploadMessage.class));
    }
}