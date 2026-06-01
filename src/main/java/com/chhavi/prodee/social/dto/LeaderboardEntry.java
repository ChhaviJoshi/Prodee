package com.chhavi.prodee.social.dto;

public record LeaderboardEntry(
        int rank,
        Long userId,
        String username,
        int dailyScore,
        int weeklyScore,
        int firstPlaceFinishes,
        int level
) {}
