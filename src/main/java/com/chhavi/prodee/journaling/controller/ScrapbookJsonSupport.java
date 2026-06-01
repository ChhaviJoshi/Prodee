package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.journaling.dto.ScrapbookRequest;
import com.chhavi.prodee.journaling.dto.StickerPlacementDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

final class ScrapbookJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ScrapbookJsonSupport() {
    }

    static ScrapbookRequest requestFrom(String title, String content, String placedStickers) {
        return new ScrapbookRequest(title, content, parsePlacedStickers(placedStickers));
    }

    private static List<StickerPlacementDto> parsePlacedStickers(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(raw, new TypeReference<List<StickerPlacementDto>>() {});
        } catch (Exception e) {
            throw new BadRequestException("Invalid placedStickers JSON: " + e.getMessage());
        }
    }
}
