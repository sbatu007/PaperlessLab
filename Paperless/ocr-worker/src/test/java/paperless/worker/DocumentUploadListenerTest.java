package paperless.worker;

import org.junit.jupiter.api.Test;
import paperless.worker.rabbitmq.DocumentUploadListener;
import paperless.worker.rabbitmq.DocumentUploadMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

public class DocumentUploadListenerTest {

    @Test
    void handle_doesNotThrow() {
        var listener = new DocumentUploadListener();
        var message = new DocumentUploadMessage(1L, "test.pdf");

        assertThatCode(() -> listener.handle(message)).doesNotThrowAnyException();
    }
}
