package com.chhavi.prodee.productivity.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.gamification.event.TaskCompletedEvent;
import com.chhavi.prodee.productivity.dto.TaskRequest;
import com.chhavi.prodee.productivity.dto.TaskResponse;
import com.chhavi.prodee.productivity.entity.Task;
import com.chhavi.prodee.productivity.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskResponse createTask(String username, TaskRequest request) {
        User user = findUser(username);
        Task task = Task.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .difficulty(request.difficulty())
                .tags(request.tags())
                .dueDate(request.dueDate())
                .build();
        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getUserTasks(String username) {
        User user = findUser(username);
        return taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(String username, Long taskId) {
        Task task = findTaskForUser(username, taskId);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(String username, Long taskId, TaskRequest request) {
        Task task = findTaskForUser(username, taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDifficulty(request.difficulty());
        task.setTags(request.tags());
        task.setDueDate(request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(String username, Long taskId) {
        Task task = findTaskForUser(username, taskId);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse completeTask(String username, Long taskId) {
        Task task = findTaskForUser(username, taskId);
        if (task.isCompleted()) {
            throw new BadRequestException("Task is already completed");
        }
        task.setCompleted(true);
        task.setCompletedAt(Instant.now());
        Task saved = taskRepository.save(task);

        // Fire event for gamification
        eventPublisher.publishEvent(new TaskCompletedEvent(this, saved));

        return toResponse(saved);
    }

    /**
     * Return all tasks for a given user (used by Cohort views)
     */
    public List<TaskResponse> getPublicTasks(Long userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Task findTaskForUser(String username, Long taskId) {
        User user = findUser(username);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Task does not belong to the current user");
        }
        return task;
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(), t.getTitle(), t.getDescription(), t.getDifficulty(),
                t.isCompleted(), t.getTags(),
                t.getDueDate(), t.getCompletedAt(), t.getCreatedAt());
    }
}
