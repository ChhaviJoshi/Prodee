package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.journaling.dto.DailyAnalyticsRequest;
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
 * Integration tests for {@code /api/journal/analytics}.
 * Covers daily logging (upsert), weekly/monthly chart endpoints,
 * validation, and authorization.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("DailyAnalyticsController Integration Tests")
class DailyAnalyticsControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/journal/analytics";

    // ── LOG ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/journal/analytics")
    @WithMockUser(username = "testuser", roles = "USER")
    class Log {

        @Test
        @DisplayName("201 — valid analytics entry is created")
        void givenValidLog_whenCreate_thenReturn201() throws Exception {
            DailyAnalyticsRequest req = new DailyAnalyticsRequest(
                    LocalDate.now(), 7.5, 120, 90);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.sleepHours").value(7.5))
                    .andExpect(jsonPath("$.data.screenTimeMinutes").value(120))
                    .andExpect(jsonPath("$.data.productivityFocusMinutes").value(90));
        }

        @Test
        @DisplayName("201 — posting same day again performs upsert (updates existing)")
        void givenSameDate_whenCreateAgain_thenUpsert() throws Exception {
            DailyAnalyticsRequest first = new DailyAnalyticsRequest(
                    LocalDate.now(), 6.0, 200, 60);
            DailyAnalyticsRequest second = new DailyAnalyticsRequest(
                    LocalDate.now(), 8.0, 100, 120);

            // First log
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(first)))
                    .andExpect(status().isCreated());

            // Second log (same date — should upsert)
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(second)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.sleepHours").value(8.0))
                    .andExpect(jsonPath("$.data.screenTimeMinutes").value(100));

            // Only one entry for today
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }

        @Test
        @DisplayName("400 — null date triggers validation error")
        void givenNullDate_whenCreate_thenReturn400() throws Exception {
            String json = """
                    {
                      "date": null,
                      "sleepHours": 7,
                      "screenTimeMinutes": 120,
                      "productivityFocusMinutes": 90
                    }
                    """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — negative sleep hours triggers validation error")
        void givenNegativeSleep_whenCreate_thenReturn400() throws Exception {
            String json = """
                    {
                      "date": "2026-03-05",
                      "sleepHours": -1,
                      "screenTimeMinutes": 120,
                      "productivityFocusMinutes": 90
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
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            DailyAnalyticsRequest req = new DailyAnalyticsRequest(
                    LocalDate.now(), 7.0, 120, 90);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/journal/analytics")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all analytics logs")
        void givenLogs_whenGetAll_thenReturnList() throws Exception {
            logAnalytics(LocalDate.now());
            logAnalytics(LocalDate.now().minusDays(1));

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/journal/analytics/weekly")
    @WithMockUser(username = "testuser", roles = "USER")
    class Weekly {

        @Test
        @DisplayName("200 — returns logs from past 7 days (chart-friendly)")
        void givenRecentLogs_whenGetWeekly_thenReturnFiltered() throws Exception {
            logAnalytics(LocalDate.now());
            logAnalytics(LocalDate.now().minusDays(3));

            mockMvc.perform(get(BASE + "/weekly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].date").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].sleepHours").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/journal/analytics/monthly")
    @WithMockUser(username = "testuser", roles = "USER")
    class Monthly {

        @Test
        @DisplayName("200 — returns logs from past 30 days")
        void givenLogs_whenGetMonthly_thenReturnList() throws Exception {
            logAnalytics(LocalDate.now());

            mockMvc.perform(get(BASE + "/monthly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private void logAnalytics(LocalDate date) throws Exception {
        DailyAnalyticsRequest req = new DailyAnalyticsRequest(date, 7.0, 120, 90);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated());
    }
}
