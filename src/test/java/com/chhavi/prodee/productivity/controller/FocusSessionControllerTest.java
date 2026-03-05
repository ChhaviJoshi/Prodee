package com.chhavi.prodee.productivity.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.productivity.dto.FocusSessionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/focus-sessions} (Focus Island / Pomodoro).
 * Covers session logging, efficiency score calculation, weekly analytics, and validation.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("FocusSessionController Integration Tests")
class FocusSessionControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/focus-sessions";

    // ── LOG SESSION ─────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/focus-sessions")
    @WithMockUser(username = "testuser", roles = "USER")
    class LogSession {

        @Test
        @DisplayName("201 — valid session is logged with efficiency score")
        void givenValidSession_whenLog_thenReturn201() throws Exception {
            Instant start = Instant.now().minusSeconds(1500); // 25 min ago
            Instant end = Instant.now();

            FocusSessionRequest req = new FocusSessionRequest(25, 25, "lofi", start, end);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.expectedDurationMinutes").value(25))
                    .andExpect(jsonPath("$.data.actualDurationMinutes").value(25))
                    .andExpect(jsonPath("$.data.efficiencyScore").isNumber())
                    .andExpect(jsonPath("$.data.ambientType").value("lofi"));
        }

        @Test
        @DisplayName("201 — session without ambient type is logged")
        void givenNoAmbient_whenLog_thenReturn201() throws Exception {
            Instant start = Instant.now().minusSeconds(600);
            Instant end = Instant.now();

            FocusSessionRequest req = new FocusSessionRequest(10, 8, null, start, end);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.ambientType").isEmpty());
        }

        @Test
        @DisplayName("400 — zero expected duration triggers validation error")
        void givenZeroDuration_whenLog_thenReturn400() throws Exception {
            String json = """
                    {
                      "expectedDurationMinutes": 0,
                      "actualDurationMinutes": 10,
                      "startedAt": "2026-03-01T10:00:00Z",
                      "endedAt": "2026-03-01T10:10:00Z"
                    }
                    """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — null startedAt triggers validation error")
        void givenNullStart_whenLog_thenReturn400() throws Exception {
            String json = """
                    {
                      "expectedDurationMinutes": 25,
                      "actualDurationMinutes": 25,
                      "startedAt": null,
                      "endedAt": "2026-03-01T10:25:00Z"
                    }
                    """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenLog_thenReturn401() throws Exception {
            String json = """
                    {
                      "expectedDurationMinutes": 25,
                      "actualDurationMinutes": 25,
                      "startedAt": "2026-03-01T10:00:00Z",
                      "endedAt": "2026-03-01T10:25:00Z"
                    }
                    """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/focus-sessions")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all sessions for user")
        void givenSessions_whenGetAll_thenReturnList() throws Exception {
            logSession(25, 25);
            logSession(10, 8);

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("200 — returns empty list when no sessions")
        void givenNoSessions_whenGetAll_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/focus-sessions/weekly")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadWeekly {

        @Test
        @DisplayName("200 — returns sessions from past 7 days")
        void givenRecentSessions_whenGetWeekly_thenReturnList() throws Exception {
            logSession(25, 25);

            mockMvc.perform(get(BASE + "/weekly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private void logSession(int expected, int actual) throws Exception {
        Instant start = Instant.now().minusSeconds(expected * 60L);
        Instant end = Instant.now();

        FocusSessionRequest req = new FocusSessionRequest(expected, actual, "lofi", start, end);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated());
    }
}
