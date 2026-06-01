package com.chhavi.prodee.journaling.dto;

import java.time.LocalDate;

public record DailyAnalyticsResponse(
        Long id,
        LocalDate date,
        Double sleepHours,
        Double screenTimeHours,
        Integer waterGlasses,
        Integer exerciseMinutes
) {}
