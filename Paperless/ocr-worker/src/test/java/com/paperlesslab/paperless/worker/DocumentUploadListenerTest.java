package com.paperlesslab.paperless.worker;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.worker.rabbitmq.DocumentUploadListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DocumentUploadListenerTest {

    @Test
    void handle_doesNotThrow() {
        DocumentUploadListener listener = new DocumentUploadListener();
        DocumentUploadMessage message = new DocumentUploadMessage(
                1L,
                "test.pdf",
                "demo"
        );

        assertDoesNotThrow(() -> listener.handle(message));
    }
}
