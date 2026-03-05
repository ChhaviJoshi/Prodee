package com.chhavi.prodee.productivity.dto;

import java.time.Instant;

public record FocusSessionResponse(
        Long id,
        int expectedDurationMinutes,
        int actualDurationMinutes,
        double efficiencyScore,
        String ambientType,
        Instant startedAt,
        Instant endedAt
) {}
