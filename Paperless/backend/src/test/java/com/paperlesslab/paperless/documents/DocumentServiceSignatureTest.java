package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentServiceSignatureTest {

    @Test
    void publicServiceMethods_doNotUseDtoInSignature() {
        Method[] methods = DocumentService.class.getMethods();
        boolean anyDtoInParams = Arrays.stream(methods)
                .anyMatch(m -> Arrays.stream(m.getParameterTypes())
                        .anyMatch(t -> t.equals(DocumentDto.class)));

        boolean anyDtoAsReturn = Arrays.stream(methods)
                .anyMatch(m -> m.getReturnType().equals(DocumentDto.class));

        assertThat(anyDtoInParams)
                .as("DocumentService darf keine DTOs als Parameter verwenden")
                .isFalse();

        assertThat(anyDtoAsReturn)
                .as("DocumentService darf keine DTOs als Return-Type verwenden")
                .isFalse();
    }
}
