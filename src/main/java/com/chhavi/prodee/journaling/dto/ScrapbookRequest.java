package com.chhavi.prodee.journaling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScrapbookRequest(
        @NotBlank @Size(max = 200) String title,
        String content
) {}
