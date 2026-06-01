package com.chhavi.prodee.productivity.dto;

import com.chhavi.prodee.productivity.entity.TaskDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotNull TaskDifficulty difficulty,
        String tags,
        LocalDate dueDate
) {}
