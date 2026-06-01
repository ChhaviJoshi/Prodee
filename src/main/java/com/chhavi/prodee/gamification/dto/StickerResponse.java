package com.chhavi.prodee.gamification.dto;

public record StickerResponse(
        Long id,
        String name,
        String imageUrl,
        int price
) {}
