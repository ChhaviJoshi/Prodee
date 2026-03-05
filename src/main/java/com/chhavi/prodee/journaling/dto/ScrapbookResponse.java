package com.chhavi.prodee.journaling.dto;

import java.time.Instant;

public record ScrapbookResponse(
        Long id,
        String title,
        String content,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {}
