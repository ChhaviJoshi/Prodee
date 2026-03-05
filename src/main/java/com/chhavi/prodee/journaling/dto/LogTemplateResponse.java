package com.chhavi.prodee.journaling.dto;

import java.time.Instant;

public record LogTemplateResponse(
        Long id,
        String name,
        String colorMapping,
        Instant createdAt
) {}
