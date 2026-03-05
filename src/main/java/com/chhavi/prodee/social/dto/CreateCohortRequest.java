package com.chhavi.prodee.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCohortRequest(
        @NotBlank @Size(max = 100) String name
) {}
