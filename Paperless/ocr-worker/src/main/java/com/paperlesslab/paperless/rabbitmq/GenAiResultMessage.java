package com.paperlesslab.paperless.rabbitmq;

import java.io.Serializable;

public record GenAiResultMessage(
        Long documentId,
        String result,
        String ocrText
) implements Serializable {}
