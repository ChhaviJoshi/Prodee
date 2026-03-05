package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.journaling.dto.DailyAnalyticsRequest;
import com.chhavi.prodee.journaling.dto.DailyAnalyticsResponse;
import com.chhavi.prodee.journaling.service.DailyAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal/analytics")
@RequiredArgsConstructor
@Tag(name = "Daily Analytics", description = "Quantitative daily tracking: sleep, screen time, focus — for charts and AI insights")
public class DailyAnalyticsController {

    private final DailyAnalyticsService dailyAnalyticsService;

    @PostMapping
    @Operation(summary = "Log daily analytics (upsert — one entry per user per day)")
    public ResponseEntity<ApiResponse<DailyAnalyticsResponse>> log(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody DailyAnalyticsRequest request) {
        DailyAnalyticsResponse entry = dailyAnalyticsService.logAnalytics(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Analytics log saved", entry));
    }

    @GetMapping
    @Operation(summary = "Get all analytics logs")
    public ResponseEntity<ApiResponse<List<DailyAnalyticsResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(dailyAnalyticsService.getLogs(user.getUsername())));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get analytics for the past 7 days (optimized for Recharts / Chart.js)")
    public ResponseEntity<ApiResponse<List<DailyAnalyticsResponse>>> getWeekly(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(dailyAnalyticsService.getWeeklyLogs(user.getUsername())));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get analytics for the past 30 days (optimized for Recharts / Chart.js)")
    public ResponseEntity<ApiResponse<List<DailyAnalyticsResponse>>> getMonthly(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(dailyAnalyticsService.getMonthlyLogs(user.getUsername())));
    }
}
