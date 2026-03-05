package com.chhavi.prodee.productivity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HabitRequest(
        @NotBlank @Size(max = 200) String title,
        String tag,
        @NotBlank String frequency
) {}
