package com.chhavi.prodee.gamification.dto;

import com.chhavi.prodee.gamification.entity.ItemCategory;

public record ShopItemResponse(
        Long id,
        String name,
        String description,
        ItemCategory category,
        int price,
        String imageUrl,
        int levelRequired
) {}
