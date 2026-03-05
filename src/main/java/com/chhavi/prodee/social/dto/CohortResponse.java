package com.chhavi.prodee.social.dto;

import java.time.Instant;
import java.util.List;

public record CohortResponse(
        Long id,
        String name,
        String joinCode,
        List<CohortMemberInfo> members,
        Instant createdAt
) {}
