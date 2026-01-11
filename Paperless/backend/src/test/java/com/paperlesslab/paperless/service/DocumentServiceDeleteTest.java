package com.paperlesslab.paperless.service;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.minio.FileStorageService;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.repository.DocumentRepository;
import com.paperlesslab.paperless.repository.LabelRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class DocumentServiceDeleteTest {

    @Test
    void delete_removesDb_andCallsMinioAndIndexDelete() {
        DocumentRepository repo = mock(DocumentRepository.class);
        RabbitMqProducer producer = mock(RabbitMqProducer.class);
        FileStorageService minio = mock(FileStorageService.class);
        SearchIndexService index = mock(SearchIndexService.class);
        LabelRepository labelRepository = mock(LabelRepository.class);

        Document doc = new Document();
        doc.setId(1L);
        doc.setFilename("HelloWorld.pdf");

        when(repo.findById(1L)).thenReturn(Optional.of(doc));

        DocumentService svc = new DocumentService(
                repo,
                producer,
                minio,
                index,
                labelRepository
        );

        svc.delete(1L);

        verify(repo).delete(doc);
        verify(minio).deletePdf("HelloWorld.pdf");
        verify(index).deleteFromIndex(1L);
    }
}
