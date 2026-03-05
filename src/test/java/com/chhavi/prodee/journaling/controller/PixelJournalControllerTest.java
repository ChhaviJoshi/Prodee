package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.journaling.dto.DailyPixelRequest;
import com.chhavi.prodee.journaling.dto.LogTemplateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/journal/pixels}.
 * Covers template creation (including color mapping validation),
 * pixel painting, yearly grid retrieval, and per-template queries.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("PixelJournalController Integration Tests")
class PixelJournalControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/journal/pixels";

    private static final String VALID_COLOR_MAPPING =
            "{\"1\":\"#1a1a1a\",\"2\":\"#ff4500\",\"3\":\"#ffd700\",\"4\":\"#32cd32\"}";

    // ╔══════════════════════════════════════════════════════╗
    // ║                   TEMPLATES                         ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/journal/pixels/templates")
    @WithMockUser(username = "testuser", roles = "USER")
    class CreateTemplate {

        @Test
        @DisplayName("201 — valid template is created")
        void givenValidTemplate_whenCreate_thenReturn201() throws Exception {
            LogTemplateRequest req = new LogTemplateRequest("Mood", VALID_COLOR_MAPPING);

            mockMvc.perform(post(BASE + "/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.name").value("Mood"))
                    .andExpect(jsonPath("$.data.colorMapping").isNotEmpty());
        }

        @Test
        @DisplayName("400 — blank name triggers validation error")
        void givenBlankName_whenCreate_thenReturn400() throws Exception {
            LogTemplateRequest req = new LogTemplateRequest("", VALID_COLOR_MAPPING);

            mockMvc.perform(post(BASE + "/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — blank color mapping triggers validation error")
        void givenBlankColorMapping_whenCreate_thenReturn400() throws Exception {
            LogTemplateRequest req = new LogTemplateRequest("Bad Template", "");

            mockMvc.perform(post(BASE + "/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — plain text color mapping is rejected (must be JSON)")
        void givenPlainTextMapping_whenCreate_thenReturn400() throws Exception {
            LogTemplateRequest req = new LogTemplateRequest("Invalid", "not-json");

            mockMvc.perform(post(BASE + "/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/journal/pixels/templates")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadTemplates {

        @Test
        @DisplayName("200 — returns all templates for the user")
        void givenTemplates_whenGetAll_thenReturnList() throws Exception {
            createTemplate("Mood");
            createTemplate("Energy");

            mockMvc.perform(get(BASE + "/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                    PIXELS                           ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/journal/pixels")
    @WithMockUser(username = "testuser", roles = "USER")
    class PaintPixel {

        @Test
        @DisplayName("201 — pixel painted with valid intensity and resolved color")
        void givenValidPixel_whenPaint_thenReturn201() throws Exception {
            Long templateId = createTemplate("Mood");

            DailyPixelRequest req = new DailyPixelRequest(templateId, LocalDate.now(), 2);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.templateName").value("Mood"))
                    .andExpect(jsonPath("$.data.intensity").value(2))
                    .andExpect(jsonPath("$.data.colorHex").value("#ff4500"));
        }

        @Test
        @DisplayName("400 — intensity 0 is rejected (min 1)")
        void givenZeroIntensity_whenPaint_thenReturn400() throws Exception {
            Long templateId = createTemplate("Mood");

            DailyPixelRequest req = new DailyPixelRequest(templateId, LocalDate.now(), 0);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 — painting with non-existent template ID fails")
        void givenBadTemplateId_whenPaint_thenReturn404() throws Exception {
            DailyPixelRequest req = new DailyPixelRequest(99999L, LocalDate.now(), 1);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/journal/pixels/year/{year}")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadByYear {

        @Test
        @DisplayName("200 — returns pixels for the given year")
        void givenPixels_whenGetByYear_thenReturnList() throws Exception {
            Long templateId = createTemplate("Mood");
            paintPixel(templateId, LocalDate.now(), 3);

            mockMvc.perform(get(BASE + "/year/" + LocalDate.now().getYear()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].intensity").value(3));
        }

        @Test
        @DisplayName("200 — returns empty for a year with no pixels")
        void givenNoPixels_whenGetByYear_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE + "/year/2020"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/journal/pixels/template/{templateId}")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadByTemplate {

        @Test
        @DisplayName("200 — returns pixels for a specific template")
        void givenPixels_whenGetByTemplate_thenReturnList() throws Exception {
            Long templateId = createTemplate("Mood");
            paintPixel(templateId, LocalDate.now(), 1);
            paintPixel(templateId, LocalDate.now().minusDays(1), 4);

            mockMvc.perform(get(BASE + "/template/" + templateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Long createTemplate(String name) throws Exception {
        LogTemplateRequest req = new LogTemplateRequest(name, VALID_COLOR_MAPPING);

        String body = mockMvc.perform(post(BASE + "/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private void paintPixel(Long templateId, LocalDate date, int intensity) throws Exception {
        DailyPixelRequest req = new DailyPixelRequest(templateId, date, intensity);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated());
    }
}
