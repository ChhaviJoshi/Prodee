package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.productivity.dto.TaskRequest;
import com.chhavi.prodee.productivity.entity.TaskDifficulty;
import com.chhavi.prodee.productivity.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for {@code /api/tasks}.
 * Covers CRUD, completion with gamification rewards, validation,
 * authorization, and cross-user ownership isolation.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("TaskController Integration Tests")
class TaskControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/tasks";

    @Autowired
    private TaskRepository taskRepository;

    // ╔══════════════════════════════════════════════════════╗
    // ║                     CREATE                          ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/tasks")
    @WithMockUser(username = "testuser", roles = "USER")
    class Create {

        @Test
        @DisplayName("201 — valid task is created and returned")
        void givenValidTask_whenCreate_thenReturn201() throws Exception {
            TaskRequest req = new TaskRequest(
                    "Study Spring Boot", "Chapter 5", TaskDifficulty.MEDIUM,
                    "java,spring", LocalDate.now().plusDays(3));

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.title").value("Study Spring Boot"))
                    .andExpect(jsonPath("$.data.difficulty").value("MEDIUM"))
                    .andExpect(jsonPath("$.data.completed").value(false))
                    .andExpect(jsonPath("$.data.tags").value("java,spring"));
        }

        @Test
        @DisplayName("201 — minimal task (only required fields)")
        void givenMinimalTask_whenCreate_thenReturn201() throws Exception {
            TaskRequest req = new TaskRequest(
                    "Quick task", null, TaskDifficulty.EASY,
                    null, null);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("Quick task"))
                    .andExpect(jsonPath("$.data.difficulty").value("EASY"));
        }

        @Test
        @DisplayName("400 — blank title triggers validation error")
        void givenBlankTitle_whenCreate_thenReturn400() throws Exception {
            TaskRequest req = new TaskRequest(
                    "", null, TaskDifficulty.EASY,
                    null, null);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("400 — null difficulty triggers validation error")
        void givenNullDifficulty_whenCreate_thenReturn400() throws Exception {
            String json = """
                    { "title": "No difficulty", "difficulty": null }
                    """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            TaskRequest req = new TaskRequest(
                    "Unauthorized task", null, TaskDifficulty.EASY,
                    null, null);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                      READ                           ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("GET /api/tasks")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all tasks for the authenticated user")
        void givenTasks_whenGetAll_thenReturnList() throws Exception {
            // Seed two tasks
            createTask("Task A", TaskDifficulty.EASY);
            createTask("Task B", TaskDifficulty.HARD);

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("200 — returns empty list when user has no tasks")
        void givenNoTasks_whenGetAll_thenReturnEmptyList() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadById {

        @Test
        @DisplayName("200 — returns a single task by ID")
        void givenExistingTask_whenGetById_thenReturn200() throws Exception {
            Long taskId = createTask("My task", TaskDifficulty.MEDIUM);

            mockMvc.perform(get(BASE + "/" + taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(taskId))
                    .andExpect(jsonPath("$.data.title").value("My task"));
        }

        @Test
        @DisplayName("404 — non-existent ID returns not found")
        void givenNonExistentId_whenGetById_thenReturn404() throws Exception {
            mockMvc.perform(get(BASE + "/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — accessing another user's task is rejected")
        void givenOtherUsersTask_whenGetById_thenReturn400() throws Exception {
            // Create task owned by "otheruser"
            User other = createOtherUser();
            Long taskId = createTaskForUser(other, "Other's task", TaskDifficulty.EASY);

            // "testuser" tries to access it
            mockMvc.perform(get(BASE + "/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                     UPDATE                          ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("PUT /api/tasks/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Update {

        @Test
        @DisplayName("200 — task is updated successfully")
        void givenValidUpdate_whenUpdate_thenReturn200() throws Exception {
            Long taskId = createTask("Original title", TaskDifficulty.EASY);

            TaskRequest updated = new TaskRequest(
                    "Updated title", "New description", TaskDifficulty.HARD,
                    "updated", LocalDate.now().plusDays(7));

            mockMvc.perform(put(BASE + "/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Updated title"))
                    .andExpect(jsonPath("$.data.difficulty").value("HARD"))
                    .andExpect(jsonPath("$.data.recurring").value(true));
        }

        @Test
        @DisplayName("404 — updating non-existent task returns not found")
        void givenNonExistentId_whenUpdate_thenReturn404() throws Exception {
            TaskRequest req = new TaskRequest(
                    "Ghost", null, TaskDifficulty.EASY,
                    null, null);

            mockMvc.perform(put(BASE + "/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("400 — blank title in update triggers validation")
        void givenBlankTitle_whenUpdate_thenReturn400() throws Exception {
            Long taskId = createTask("Valid task", TaskDifficulty.EASY);

            TaskRequest bad = new TaskRequest(
                    "", null, TaskDifficulty.EASY,
                    null, null);

            mockMvc.perform(put(BASE + "/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(bad)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                     DELETE                          ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Delete {

        @Test
        @DisplayName("200 — task is deleted")
        void givenExistingTask_whenDelete_thenReturn200() throws Exception {
            Long taskId = createTask("Deletable", TaskDifficulty.EASY);

            mockMvc.perform(delete(BASE + "/" + taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify it's gone
            mockMvc.perform(get(BASE + "/" + taskId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 — deleting non-existent task returns not found")
        void givenNonExistentId_whenDelete_thenReturn404() throws Exception {
            mockMvc.perform(delete(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                    COMPLETE                         ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/tasks/{id}/complete")
    @WithMockUser(username = "testuser", roles = "USER")
    class Complete {

        @Test
        @DisplayName("200 — completing task marks it done")
        void givenIncompleteTask_whenComplete_thenReturn200() throws Exception {
            Long taskId = createTask("Completable", TaskDifficulty.MEDIUM);

            mockMvc.perform(post(BASE + "/" + taskId + "/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.completed").value(true))
                    .andExpect(jsonPath("$.data.completedAt").isNotEmpty());
        }

        @Test
        @DisplayName("404 — completing non-existent task returns not found")
        void givenNonExistentId_whenComplete_thenReturn404() throws Exception {
            mockMvc.perform(post(BASE + "/99999/complete"))
                    .andExpect(status().isNotFound());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                   HELPERS                           ║
    // ╚══════════════════════════════════════════════════════╝

    /** Create a task via the API and return its ID. */
    private Long createTask(String title, TaskDifficulty difficulty) throws Exception {
        TaskRequest req = new TaskRequest(title, null, difficulty, null, null);

        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /** Create a task directly in the DB for a specific user (bypasses auth). */
    private Long createTaskForUser(User user, String title, TaskDifficulty difficulty) {
        var task = com.chhavi.prodee.productivity.entity.Task.builder()
                .user(user)
                .title(title)
                .difficulty(difficulty)
                .build();
        return taskRepository.save(task).getId();
    }
}
