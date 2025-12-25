package com.paperlesslab.paperless.rabbitmq;

import java.io.Serializable;

public record GenAiResultMessage(
        Long documentId,
        String ocrText,
        String result
) implements Serializable {}
