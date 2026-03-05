package com.chhavi.prodee.auth.dto;

import java.time.Instant;
import java.util.Set;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        int xp,
        int level,
        int coins,
        Set<String> roles,
        Instant createdAt
) {}
