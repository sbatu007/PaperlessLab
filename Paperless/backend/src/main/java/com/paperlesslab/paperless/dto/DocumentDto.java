package com.paperlesslab.paperless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentDto(
        Long id,
        @NotBlank String filename,
        @Size(max = 2000, message = "Description max. 2000 letters")
        String description
) {}
