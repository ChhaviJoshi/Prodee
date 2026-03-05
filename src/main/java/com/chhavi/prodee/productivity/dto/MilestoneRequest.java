package com.chhavi.prodee.productivity.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MilestoneRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @Future LocalDate targetDate
) {}
