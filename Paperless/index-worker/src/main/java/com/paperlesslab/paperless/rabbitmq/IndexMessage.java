package com.paperlesslab.paperless.rabbitmq;

public record IndexMessage(
        Long documentId,
        String filename,
        String ocrText
) { }
