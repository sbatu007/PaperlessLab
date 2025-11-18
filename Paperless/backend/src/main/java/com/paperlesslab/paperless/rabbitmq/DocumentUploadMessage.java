package com.paperlesslab.paperless.rabbitmq;

import java.io.Serializable;

public record DocumentUploadMessage(
        Long documentId,
        String filename
) implements Serializable {
}
