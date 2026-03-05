package com.chhavi.prodee.productivity.dto;

import java.time.LocalDate;
import java.util.List;

public record MilestoneResponse(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate targetDate,
        long totalDays,
        long daysPassed,
        long daysRemaining,
        List<Boolean> grid
) {}
