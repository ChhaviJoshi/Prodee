package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StickerPlacementDto(
        @NotNull Long stickerId,
        @NotNull @Min(0) Integer x,
        @NotNull @Min(0) Integer y
) {}
