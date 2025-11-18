package paperless.worker;

import org.junit.jupiter.api.Test;
import paperless.worker.rabbitmq.DocumentUploadListener;
import paperless.worker.rabbitmq.DocumentUploadMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
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
