package com.chhavi.prodee.productivity.dto;

import java.time.Instant;

public record ArticleResponse(
        Long id,
        String title,
        String url,
        String source,
        String tags,
        String coverImageUrl,
        Instant fetchedAt
) {}
