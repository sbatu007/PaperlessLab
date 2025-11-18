package com.paperlesslab.paperless.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentDto(
        Long id,
        @NotBlank String filename,
        String description
) {}
