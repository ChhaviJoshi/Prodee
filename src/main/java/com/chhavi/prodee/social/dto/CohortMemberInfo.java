package com.chhavi.prodee.social.dto;

import com.chhavi.prodee.social.entity.CohortRole;

public record CohortMemberInfo(
        Long userId,
        String username,
        CohortRole role,
        int dailyScore,
        int level,
        int xp
) {}
