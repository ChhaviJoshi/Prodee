package com.chhavi.prodee.productivity.dto;

import java.time.Instant;

public record HabitResponse(
        Long id,
        String title,
        String tag,
        String frequency,
        int streak,
        boolean active,
        Instant createdAt
) {}
