package com.chhavi.prodee.social.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.social.dto.CreateCohortRequest;
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
 * Integration tests for {@code /api/cohorts}.
 * Covers cohort creation, joining, leaderboard, member kicking,
 * and authorization / ownership isolation.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("CohortController Integration Tests")
class CohortControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/cohorts";

    // ── CREATE ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/cohorts")
    @WithMockUser(username = "testuser", roles = "USER")
    class Create {

        @Test
        @DisplayName("201 — cohort created with join code, creator becomes ADMIN")
        void givenValidCohort_whenCreate_thenReturn201() throws Exception {
            CreateCohortRequest req = new CreateCohortRequest("Study Squad");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.name").value("Study Squad"))
                    .andExpect(jsonPath("$.data.joinCode").isNotEmpty())
                    .andExpect(jsonPath("$.data.members", hasSize(1)))
                    .andExpect(jsonPath("$.data.members[0].username").value("testuser"))
                    .andExpect(jsonPath("$.data.members[0].role").value("ADMIN"));
        }

        @Test
        @DisplayName("400 — blank name triggers validation error")
        void givenBlankName_whenCreate_thenReturn400() throws Exception {
            CreateCohortRequest req = new CreateCohortRequest("");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenCreate_thenReturn401() throws Exception {
            CreateCohortRequest req = new CreateCohortRequest("Secret Squad");

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── JOIN ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/cohorts/join/{joinCode}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Join {

        @Test
        @DisplayName("200 — second user joins cohort via join code")
        void givenJoinCode_whenJoin_thenReturn200() throws Exception {
            String joinCode = createCohortAndGetJoinCode("Joinable Squad");

            // Create and switch to another user
            createOtherUser();

            // Other user joins — we need to mock as other user
            mockMvc.perform(post(BASE + "/join/" + joinCode)
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                    .user("otheruser").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.members", hasSize(2)));
        }

        @Test
        @DisplayName("404 — invalid join code returns error")
        void givenInvalidJoinCode_whenJoin_thenReturnError() throws Exception {
            mockMvc.perform(post(BASE + "/join/INVALIDCODE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── READ ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/cohorts")
    @WithMockUser(username = "testuser", roles = "USER")
    class Read {

        @Test
        @DisplayName("200 — returns cohort details by ID")
        void givenExistingCohort_whenGetById_thenReturn200() throws Exception {
            Long cohortId = createCohort("Readable Squad");

            mockMvc.perform(get(BASE + "/" + cohortId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Readable Squad"))
                    .andExpect(jsonPath("$.data.joinCode").isNotEmpty());
        }

        @Test
        @DisplayName("200 — returns all cohorts user belongs to")
        void givenMembership_whenGetMine_thenReturnList() throws Exception {
            createCohort("Squad A");
            createCohort("Squad B");

            mockMvc.perform(get(BASE + "/mine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("200 — returns empty list for user with no cohorts")
        void givenNoMembership_whenGetMine_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE + "/mine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // ── LEADERBOARD ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/cohorts/{cohortId}/leaderboard")
    @WithMockUser(username = "testuser", roles = "USER")
    class Leaderboard {

        @Test
        @DisplayName("200 — returns leaderboard for the cohort")
        void givenCohort_whenGetLeaderboard_thenReturnSorted() throws Exception {
            Long cohortId = createCohort("Leaderboard Squad");

            mockMvc.perform(get(BASE + "/" + cohortId + "/leaderboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].username").value("testuser"))
                    .andExpect(jsonPath("$.data[0].rank").value(1));
        }
    }

    // ── KICK ────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/cohorts/{cohortId}/members/{userId}")
    @WithMockUser(username = "testuser", roles = "USER")
    class KickMember {

        @Test
        @DisplayName("200 — admin can kick a member")
        void givenAdmin_whenKickMember_thenReturn200() throws Exception {
            String joinCode = createCohortAndGetJoinCode("Kickable Squad");
            Long cohortId = getCohortIdByJoinCode(joinCode);

            // Join as other user
            User other = createOtherUser();
            mockMvc.perform(post(BASE + "/join/" + joinCode)
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                    .user("otheruser").roles("USER")))
                    .andExpect(status().isOk());

            // testuser (admin) kicks otheruser
            mockMvc.perform(delete(BASE + "/" + cohortId + "/members/" + other.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Long createCohort(String name) throws Exception {
        CreateCohortRequest req = new CreateCohortRequest(name);

        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private String createCohortAndGetJoinCode(String name) throws Exception {
        CreateCohortRequest req = new CreateCohortRequest(name);

        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("joinCode").asText();
    }

    private Long getCohortIdByJoinCode(String joinCode) throws Exception {
        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CreateCohortRequest("Lookup"))))
                .andReturn().getResponse().getContentAsString();

        // We already created it, just get the most recent cohort for the user
        String mineBody = mockMvc.perform(get(BASE + "/mine"))
                .andReturn().getResponse().getContentAsString();

        var dataNode = objectMapper.readTree(mineBody).path("data");
        for (var node : dataNode) {
            if (joinCode.equals(node.path("joinCode").asText())) {
                return node.path("id").asLong();
            }
        }
        throw new AssertionError("Cohort with joinCode " + joinCode + " not found");
    }
}
