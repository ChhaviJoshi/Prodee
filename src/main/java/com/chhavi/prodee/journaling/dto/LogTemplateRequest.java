package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogTemplateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank String colorMapping
) {}
