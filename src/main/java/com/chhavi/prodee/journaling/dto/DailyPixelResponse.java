package com.chhavi.prodee.journaling.dto;

import java.time.LocalDate;

public record DailyPixelResponse(
        Long id,
        String templateName,
        LocalDate date,
        Integer intensity,
        String colorHex
) {}
