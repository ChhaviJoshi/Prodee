package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
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
 * Integration tests for {@code /api/journal/scrapbook}.
 * Covers text-only entry creation, listing, retrieval, deletion,
 * and validation errors. Image upload is skipped (requires real Cloudinary).
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("ScrapbookController Integration Tests")
class ScrapbookControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/journal/scrapbook";

    // ── CREATE ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/journal/scrapbook")
    @WithMockUser(username = "testuser", roles = "USER")
    class Create {

        @Test
        @DisplayName("201 — text-only scrapbook entry is created")
        void givenValidEntry_whenCreate_thenReturn201() throws Exception {
            mockMvc.perform(multipart(BASE)
                            .param("title", "My Field Trip")
                            .param("content", "Had an amazing day at the museum"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.title").value("My Field Trip"))
                    .andExpect(jsonPath("$.data.content").value("Had an amazing day at the museum"))
                    .andExpect(jsonPath("$.data.imageUrl").isEmpty());
        }

        @Test
        @DisplayName("201 — entry without content is created")
        void givenTitleOnly_whenCreate_thenReturn201() throws Exception {
            mockMvc.perform(multipart(BASE)
                            .param("title", "Quick Note"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("Quick Note"));
        }

        @Test
        @DisplayName("400 — blank title triggers validation error")
        void givenBlankTitle_whenCreate_thenReturn400() throws Exception {
            mockMvc.perform(multipart(BASE)
                            .param("title", ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — Cloudinary demo key rejects image upload gracefully")
        void givenImageWithDemoKey_whenCreate_thenReturn400() throws Exception {
            byte[] dummyImage = "fake-image-bytes".getBytes();

            mockMvc.perform(multipart(BASE)
                            .file("image", dummyImage)
                            .param("title", "With Image"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message", containsString("Cloudinary")));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            mockMvc.perform(multipart(BASE)
                            .param("title", "Unauthorized"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/journal/scrapbook")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadAll {

        @Test
        @DisplayName("200 — returns all scrapbook entries")
        void givenEntries_whenGetAll_thenReturnList() throws Exception {
            createEntry("Entry A");
            createEntry("Entry B");

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("200 — returns empty list")
        void givenNoEntries_whenGetAll_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/journal/scrapbook/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class ReadById {

        @Test
        @DisplayName("200 — returns single entry")
        void givenExistingEntry_whenGetById_thenReturn200() throws Exception {
            Long id = createEntry("Single entry");

            mockMvc.perform(get(BASE + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Single entry"));
        }

        @Test
        @DisplayName("404 — non-existent entry returns not found")
        void givenNonExistentId_whenGetById_thenReturn404() throws Exception {
            mockMvc.perform(get(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE ───────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/journal/scrapbook/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Delete {

        @Test
        @DisplayName("200 — entry is deleted")
        void givenExistingEntry_whenDelete_thenReturn200() throws Exception {
            Long id = createEntry("Deletable");

            mockMvc.perform(delete(BASE + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(get(BASE + "/" + id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 — deleting non-existent entry returns not found")
        void givenNonExistentId_whenDelete_thenReturn404() throws Exception {
            mockMvc.perform(delete(BASE + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Long createEntry(String title) throws Exception {
        String body = mockMvc.perform(multipart(BASE)
                        .param("title", title)
                        .param("content", "Content for " + title))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
