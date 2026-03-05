package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.productivity.dto.HabitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/habits}.
 * Covers habit CRUD, streak completion, validation, and authorization.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("HabitController Integration Tests")
class HabitControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/habits";

    // ── CREATE ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/habits")
    @WithMockUser(username = "testuser", roles = "USER")
    class Create {

        @Test
        @DisplayName("201 — valid habit is created")
        void givenValidHabit_whenCreate_thenReturn201() throws Exception {
            HabitRequest req = new HabitRequest("Morning Run", "fitness", "DAILY");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.title").value("Morning Run"))
                    .andExpect(jsonPath("$.data.tag").value("fitness"))
                    .andExpect(jsonPath("$.data.frequency").value("DAILY"))
                    .andExpect(jsonPath("$.data.streak").value(0))
                    .andExpect(jsonPath("$.data.active").value(true));
        }

        @Test
        @DisplayName("201 — habit without optional tag is created")
        void givenNoTag_whenCreate_thenReturn201() throws Exception {
            HabitRequest req = new HabitRequest("Read 30 mins", null, "DAILY");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.tag").isEmpty());
        }

        @Test
        @DisplayName("400 — blank title triggers validation error")
        void givenBlankTitle_whenCreate_thenReturn400() throws Exception {
            HabitRequest req = new HabitRequest("", null, "DAILY");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("400 — blank frequency triggers validation error")
        void givenBlankFrequency_whenCreate_thenReturn400() throws Exception {
            HabitRequest req = new HabitRequest("Meditate", null, "");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            HabitRequest req = new HabitRequest("Unauthorized", null, "DAILY");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/habits")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all habits for the user")
        void givenHabits_whenGetAll_thenReturnList() throws Exception {
            createHabit("Habit A", "DAILY");
            createHabit("Habit B", "WEEKLY");

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("200 — empty list when user has no habits")
        void givenNoHabits_whenGetAll_thenReturnEmptyList() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // ── COMPLETE ────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/habits/{id}/complete")
    @WithMockUser(username = "testuser", roles = "USER")
    class Complete {

        @Test
        @DisplayName("200 — completing a habit increments the streak")
        void givenExistingHabit_whenComplete_thenStreakUpdated() throws Exception {
            Long habitId = createHabit("Pushups", "DAILY");

            mockMvc.perform(post(BASE + "/" + habitId + "/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.streak").value(1));
        }

        @Test
        @DisplayName("404 — completing non-existent habit returns not found")
        void givenNonExistentId_whenComplete_thenReturn404() throws Exception {
            mockMvc.perform(post(BASE + "/99999/complete"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE ───────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/habits/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Delete {

        @Test
        @DisplayName("200 — habit is deleted")
        void givenExistingHabit_whenDelete_thenReturn200() throws Exception {
            Long habitId = createHabit("Deletable", "DAILY");

            mockMvc.perform(delete(BASE + "/" + habitId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("404 — deleting non-existent habit returns not found")
        void givenNonExistentId_whenDelete_thenReturn404() throws Exception {
            mockMvc.perform(delete(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Long createHabit(String title, String frequency) throws Exception {
        HabitRequest req = new HabitRequest(title, null, frequency);

        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
