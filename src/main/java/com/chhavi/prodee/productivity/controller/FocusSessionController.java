package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.productivity.dto.FocusSessionRequest;
import com.chhavi.prodee.productivity.dto.FocusSessionResponse;
import com.chhavi.prodee.productivity.service.FocusSessionService;
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
@RequestMapping("/api/focus-sessions")
@RequiredArgsConstructor
@Tag(name = "Focus Sessions", description = "Focus Island (Pomodoro) session logging & analytics")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    @PostMapping
    @Operation(summary = "Log a completed focus session")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> log(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody FocusSessionRequest request) {
        FocusSessionResponse session = focusSessionService.logSession(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Focus session logged", session));
    }

    @GetMapping
    @Operation(summary = "Get all focus sessions")
    public ResponseEntity<ApiResponse<List<FocusSessionResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(focusSessionService.getUserSessions(user.getUsername())));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get focus sessions from the past 7 days")
    public ResponseEntity<ApiResponse<List<FocusSessionResponse>>> getWeekly(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(focusSessionService.getWeeklySessions(user.getUsername())));
    }
}
