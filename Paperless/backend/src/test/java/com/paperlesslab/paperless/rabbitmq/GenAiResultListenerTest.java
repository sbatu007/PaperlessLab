package com.paperlesslab.paperless.rabbitmq;

import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAiResultListenerTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private GenAiResultListener listener;

    @Test
    void handleGenAiResult_savesOcrTextAndSummary() {
        Document doc = new Document();
        doc.setId(1L);
        doc.setFilename("test.pdf");
        doc.setDescription("desc");
        doc.setUploadedAt(LocalDateTime.now());

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        GenAiResultMessage message = new GenAiResultMessage(1L, "OCR content", "Result");

        listener.handleGenAiResult(message);

        verify(documentRepository).findById(1L);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document saved = captor.getValue();
        assertThat(saved.getOcrText()).isEqualTo("OCR content");
        assertThat(saved.getResult()).isEqualTo("Result");
    }

    @Test
    void handleGenAiResult_throwsIfDocumentNotFound() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        GenAiResultMessage message = new GenAiResultMessage(99L, "text", "result");

        assertThatThrownBy(() -> listener.handleGenAiResult(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Document not found");

        verify(documentRepository).findById(99L);
        verify(documentRepository, never()).save(any());
    }
}
