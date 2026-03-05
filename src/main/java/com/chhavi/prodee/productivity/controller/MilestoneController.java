package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.productivity.dto.MilestoneRequest;
import com.chhavi.prodee.productivity.dto.MilestoneResponse;
import com.chhavi.prodee.productivity.service.MilestoneService;
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
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
@Tag(name = "Countdown Calendar", description = "Track milestones with a visual day-grid countdown")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @Operation(summary = "Create a new milestone countdown")
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse milestone = milestoneService.createMilestone(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Milestone created", milestone));
    }

    @GetMapping
    @Operation(summary = "Get all milestones with progress grids")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                ApiResponse.success(milestoneService.getMilestones(user.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single milestone with its progress grid")
    public ResponseEntity<ApiResponse<MilestoneResponse>> getById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(milestoneService.getMilestone(user.getUsername(), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a milestone")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        milestoneService.deleteMilestone(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted", null));
    }
}
