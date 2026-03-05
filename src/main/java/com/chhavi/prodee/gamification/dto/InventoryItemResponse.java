package com.chhavi.prodee.gamification.dto;

import java.time.Instant;

public record InventoryItemResponse(
        Long inventoryId,
        Long itemId,
        String itemName,
        String category,
        int quantity,
        Instant acquiredAt
) {}
