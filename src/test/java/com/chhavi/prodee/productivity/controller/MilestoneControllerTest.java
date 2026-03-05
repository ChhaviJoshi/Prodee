package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.productivity.dto.MilestoneRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/milestones} (Countdown Calendar).
 * Validates grid generation, date validation, CRUD, and authorization.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("MilestoneController Integration Tests")
class MilestoneControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/milestones";

    // ── CREATE ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/milestones")
    @WithMockUser(username = "testuser", roles = "USER")
    class Create {

        @Test
        @DisplayName("201 — milestone created with correct grid size and all-false grid")
        void givenValidMilestone_whenCreate_thenReturn201WithGrid() throws Exception {
            LocalDate target = LocalDate.now().plusDays(10);
            MilestoneRequest req = new MilestoneRequest("Final Exams", target);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.title").value("Final Exams"))
                    .andExpect(jsonPath("$.data.totalDays").value(10))
                    .andExpect(jsonPath("$.data.daysPassed").value(0))
                    .andExpect(jsonPath("$.data.daysRemaining").value(10))
                    .andExpect(jsonPath("$.data.grid", hasSize(10)))
                    .andExpect(jsonPath("$.data.grid[0]").value(false))
                    .andExpect(jsonPath("$.data.grid[9]").value(false));
        }

        @Test
        @DisplayName("400 — past target date is rejected")
        void givenPastTargetDate_whenCreate_thenReturn400() throws Exception {
            MilestoneRequest req = new MilestoneRequest("Expired", LocalDate.now().minusDays(5));

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — today as target date is rejected (@Future)")
        void givenTodayAsTarget_whenCreate_thenReturn400() throws Exception {
            MilestoneRequest req = new MilestoneRequest("Today", LocalDate.now());

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — blank title triggers validation error")
        void givenBlankTitle_whenCreate_thenReturn400() throws Exception {
            MilestoneRequest req = new MilestoneRequest("", LocalDate.now().plusDays(30));

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            MilestoneRequest req = new MilestoneRequest("Secret", LocalDate.now().plusDays(30));

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/milestones")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all milestones with grids")
        void givenMilestones_whenGetAll_thenReturnList() throws Exception {
            createMilestone("Exam A", 10);
            createMilestone("Exam B", 20);

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].grid").isArray())
                    .andExpect(jsonPath("$.data[1].grid").isArray());
        }

        @Test
        @DisplayName("200 — empty list when no milestones exist")
        void givenNoMilestones_whenGetAll_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/milestones/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadById {

        @Test
        @DisplayName("200 — returns single milestone with grid")
        void givenExistingMilestone_whenGetById_thenReturn200() throws Exception {
            Long id = createMilestone("My milestone", 15);

            mockMvc.perform(get(BASE + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("My milestone"))
                    .andExpect(jsonPath("$.data.totalDays").value(15))
                    .andExpect(jsonPath("$.data.grid", hasSize(15)));
        }

        @Test
        @DisplayName("404 — non-existent ID returns not found")
        void givenNonExistentId_whenGetById_thenReturn404() throws Exception {
            mockMvc.perform(get(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE ───────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/milestones/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Delete {

        @Test
        @DisplayName("200 — milestone is deleted")
        void givenExistingMilestone_whenDelete_thenReturn200() throws Exception {
            Long id = createMilestone("Deletable", 5);

            mockMvc.perform(delete(BASE + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(get(BASE + "/" + id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 — deleting non-existent milestone returns not found")
        void givenNonExistentId_whenDelete_thenReturn404() throws Exception {
            mockMvc.perform(delete(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Long createMilestone(String title, int daysAhead) throws Exception {
        MilestoneRequest req = new MilestoneRequest(title, LocalDate.now().plusDays(daysAhead));

        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
