package com.chhavi.prodee.common.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        boolean read,
        Instant createdAt
) {}
