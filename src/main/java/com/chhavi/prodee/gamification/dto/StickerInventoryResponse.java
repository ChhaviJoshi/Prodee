package com.chhavi.prodee.gamification.dto;

import java.time.Instant;

public record StickerInventoryResponse(
        Long inventoryId,
        Long stickerId,
        String stickerName,
        String imageUrl,
        int quantity,
        Instant acquiredAt
) {}
