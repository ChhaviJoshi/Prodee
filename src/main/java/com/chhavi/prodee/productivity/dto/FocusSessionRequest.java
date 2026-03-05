package com.chhavi.prodee.productivity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FocusSessionRequest(
        @NotNull @Min(1) Integer expectedDurationMinutes,
        @NotNull @Min(1) Integer actualDurationMinutes,
        String ambientType,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt
) {}
