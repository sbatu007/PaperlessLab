package com.paperlesslab.paperless.worker;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.worker.ocr.OcrService;
import com.paperlesslab.paperless.worker.rabbitmq.DocumentUploadListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class DocumentUploadListenerTest {

    @Test
    void handle_callsOcrService_andDoesNotThrow() {
        OcrService ocrService = mock(OcrService.class);
        DocumentUploadListener listener =  new DocumentUploadListener(ocrService);

        DocumentUploadMessage message = new DocumentUploadMessage(
                1L,
                "test.pdf",
                "demo"
        );

        assertDoesNotThrow(() -> listener.handle(message));
        verify(ocrService, times(1)).process(message);
    }
}
