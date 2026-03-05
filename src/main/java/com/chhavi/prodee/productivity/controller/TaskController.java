package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.productivity.dto.TaskRequest;
import com.chhavi.prodee.productivity.dto.TaskResponse;
import com.chhavi.prodee.productivity.service.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task CRUD and completion")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created", task));
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the current user")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getUserTasks(user.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(user.getUsername(), id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Task updated", taskService.updateTask(user.getUsername(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        taskService.deleteTask(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark a task as completed — triggers XP/Coin rewards")
    public ResponseEntity<ApiResponse<TaskResponse>> complete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Task completed! XP and Coins awarded.", taskService.completeTask(user.getUsername(), id)));
    }
}
