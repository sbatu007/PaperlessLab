package com.paperlesslab.paperless.dto;

import jakarta.validation.constraints.NotBlank;

public record LabelDto(
        Long id,
        @NotBlank String name
) {}
