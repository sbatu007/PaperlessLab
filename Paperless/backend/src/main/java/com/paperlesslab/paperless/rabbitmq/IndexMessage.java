package com.paperlesslab.paperless.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IndexMessage(
        Long documentId,
        String filename,
        String ocrText,
        String result
) { }
