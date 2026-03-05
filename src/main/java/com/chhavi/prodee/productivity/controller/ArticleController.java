package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.productivity.dto.ArticleResponse;
import com.chhavi.prodee.productivity.service.ArticleAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Smart Feed", description = "Intelligent article aggregator")
public class ArticleController {

    private final ArticleAggregatorService articleAggregatorService;

    @GetMapping
    @Operation(summary = "Get recent aggregated articles (global)")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getRecent() {
        return ResponseEntity.ok(ApiResponse.success(articleAggregatorService.getRecentArticles()));
    }

    @GetMapping("/my-feed")
    @Operation(summary = "Get personalized articles based on your habit tags and active task tags")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getMyFeed(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                ApiResponse.success(articleAggregatorService.getPersonalizedFeed(user.getUsername())));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get articles by tag")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getByTag(@PathVariable String tag) {
        return ResponseEntity.ok(ApiResponse.success(articleAggregatorService.getArticlesByTag(tag)));
    }
}
