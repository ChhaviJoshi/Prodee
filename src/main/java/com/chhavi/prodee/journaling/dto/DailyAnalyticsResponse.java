package com.chhavi.prodee.journaling.dto;

import java.time.LocalDate;

public record DailyAnalyticsResponse(
        Long id,
        LocalDate date,
        Double sleepHours,
        Integer screenTimeMinutes,
        Integer productivityFocusMinutes
) {}
