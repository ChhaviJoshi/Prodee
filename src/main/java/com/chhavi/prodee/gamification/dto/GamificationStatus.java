package com.chhavi.prodee.gamification.dto;

public record GamificationStatus(
        Long userId,
        String username,
        int xp,
        int level,
        int coins,
        int xpToNextLevel
) {}
