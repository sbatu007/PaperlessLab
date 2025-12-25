package com.paperlesslab.paperless.dto;

import java.io.Serializable;

public record GenAiResultMessage(Long documentId,
                                 String ocrText,
                                 String result)
        implements Serializable {
}
