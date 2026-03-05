package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.productivity.dto.HabitRequest;
import com.chhavi.prodee.productivity.dto.HabitResponse;
import com.chhavi.prodee.productivity.service.HabitService;
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
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@Tag(name = "Habits", description = "Recurring habit management")
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    @Operation(summary = "Create a new habit")
    public ResponseEntity<ApiResponse<HabitResponse>> create(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody HabitRequest request) {
        HabitResponse habit = habitService.createHabit(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Habit created", habit));
    }

    @GetMapping
    @Operation(summary = "Get all habits for the current user")
    public ResponseEntity<ApiResponse<List<HabitResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(habitService.getUserHabits(user.getUsername())));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a habit for today (records completion, updates streak, awards XP)")
    public ResponseEntity<ApiResponse<HabitResponse>> completeHabit(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Habit completed! Streak updated.", habitService.completeHabit(user.getUsername(), id)));
    }

    @PostMapping("/{id}/streak")
    @Operation(summary = "Increment streak for a habit (deprecated — use /complete instead)")
    public ResponseEntity<ApiResponse<HabitResponse>> incrementStreak(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Streak incremented!", habitService.incrementStreak(user.getUsername(), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a habit")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        habitService.deleteHabit(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Habit deleted", null));
    }
}
