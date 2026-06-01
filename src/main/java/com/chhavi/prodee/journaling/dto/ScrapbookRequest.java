package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ScrapbookRequest(
        @NotBlank @Size(max = 200) String title,
        String content,
        List<StickerPlacementDto> placedStickers
) {}
