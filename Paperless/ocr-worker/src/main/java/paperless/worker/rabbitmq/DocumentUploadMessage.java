package paperless.worker.rabbitmq;

public record DocumentUploadMessage(Long documentId,
                                    String filename) {
}
