package com.paperlesslab.paperless.rabbitmq;

public record DocumentUploadMessage(Long documentId,
                                    String filename,
                                    String description) {
}
