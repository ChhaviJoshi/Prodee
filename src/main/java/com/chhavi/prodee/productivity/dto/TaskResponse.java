package com.chhavi.prodee.productivity.dto;

import com.chhavi.prodee.productivity.entity.TaskDifficulty;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskDifficulty difficulty,
        boolean completed,
        String tags,
        LocalDate dueDate,
        Instant completedAt,
        Instant createdAt
) {}
