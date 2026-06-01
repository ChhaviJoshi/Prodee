package com.chhavi.prodee.journaling.dto;

import java.time.Instant;
import java.util.List;

public record ScrapbookResponse(
        Long id,
        String title,
        String content,
        String imageUrl,
        List<StickerPlacementDto> placedStickers,
        Instant createdAt,
        Instant updatedAt
) {}
