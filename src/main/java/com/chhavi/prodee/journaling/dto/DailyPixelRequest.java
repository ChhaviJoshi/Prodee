package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DailyPixelRequest(
        @NotNull Long templateId,
        @NotNull LocalDate date,
        @NotNull @Min(1) Integer intensity
) {}
