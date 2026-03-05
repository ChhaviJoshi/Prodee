package com.chhavi.prodee.social.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.social.dto.CohortResponse;
import com.chhavi.prodee.social.dto.CreateCohortRequest;
import com.chhavi.prodee.social.dto.LeaderboardEntry;
import com.chhavi.prodee.social.service.CohortService;
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
@RequestMapping("/api/cohorts")
@RequiredArgsConstructor
@Tag(name = "Cohorts", description = "Social cohort management and leaderboards")
public class CohortController {

    private final CohortService cohortService;

    @PostMapping
    @Operation(summary = "Create a new cohort (you become ADMIN)")
    public ResponseEntity<ApiResponse<CohortResponse>> create(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CreateCohortRequest request) {
        CohortResponse cohort = cohortService.createCohort(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cohort created! Share the join code.", cohort));
    }

    @PostMapping("/join/{joinCode}")
    @Operation(summary = "Join a cohort using its unique join code")
    public ResponseEntity<ApiResponse<CohortResponse>> join(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String joinCode) {
        CohortResponse cohort = cohortService.joinCohort(user.getUsername(), joinCode);
        return ResponseEntity.ok(ApiResponse.success("Joined cohort!", cohort));
    }

    @GetMapping("/{cohortId}")
    @Operation(summary = "Get cohort details")
    public ResponseEntity<ApiResponse<CohortResponse>> get(@PathVariable Long cohortId) {
        return ResponseEntity.ok(ApiResponse.success(cohortService.getCohort(cohortId)));
    }

    @GetMapping("/mine")
    @Operation(summary = "Get all cohorts the current user is a member of")
    public ResponseEntity<ApiResponse<List<CohortResponse>>> getMyCohorts(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(cohortService.getUserCohorts(user.getUsername())));
    }

    @GetMapping("/{cohortId}/leaderboard")
    @Operation(summary = "Get cohort leaderboard sorted by daily score")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(@PathVariable Long cohortId) {
        return ResponseEntity.ok(ApiResponse.success(cohortService.getLeaderboard(cohortId)));
    }

    @DeleteMapping("/{cohortId}/members/{userId}")
    @Operation(summary = "Kick a member from the cohort (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long cohortId,
            @PathVariable Long userId) {
        cohortService.kickMember(user.getUsername(), cohortId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member kicked", null));
    }
}
