package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DailyAnalyticsRequest(
        @NotNull LocalDate date,
        @NotNull @Min(0) Double sleepHours,
        @NotNull @Min(0) Double screenTimeHours,
        @Min(0) Integer waterGlasses,
        @Min(0) Integer exerciseMinutes
) {}
