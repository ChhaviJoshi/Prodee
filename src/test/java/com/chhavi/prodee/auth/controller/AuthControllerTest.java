package com.chhavi.prodee.auth.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.auth.dto.LoginRequest;
import com.chhavi.prodee.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/auth} (Register / Login) and
 * {@code /api/users/me} (Profile).
 * Auth endpoints are {@code permitAll}, so no {@code @WithMockUser} is needed.
 */
@DisplayName("Auth & User Controller Integration Tests")
class AuthControllerTest extends AbstractIntegrationTest {

    // ╔══════════════════════════════════════════════════════╗
    // ║                    REGISTER                         ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("201 — valid registration returns token")
        void givenValidCredentials_whenRegister_thenReturn201() throws Exception {
            RegisterRequest req = new RegisterRequest("newuser", "new@example.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.username").value("newuser"));
        }

        @Test
        @DisplayName("400/409 — duplicate username is rejected")
        void givenDuplicateUsername_whenRegister_thenReturnError() throws Exception {
            // "testuser" already exists (created in AbstractIntegrationTest @BeforeEach)
            RegisterRequest req = new RegisterRequest(TEST_USERNAME, "dup@example.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — blank username triggers validation error")
        void givenBlankUsername_whenRegister_thenReturn400() throws Exception {
            RegisterRequest req = new RegisterRequest("", "valid@mail.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — invalid email triggers validation error")
        void givenInvalidEmail_whenRegister_thenReturn400() throws Exception {
            RegisterRequest req = new RegisterRequest("validuser", "not-an-email", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — short password triggers validation error")
        void givenShortPassword_whenRegister_thenReturn400() throws Exception {
            RegisterRequest req = new RegisterRequest("shortpw", "short@mail.com", "abc");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — username too short triggers validation error")
        void givenShortUsername_whenRegister_thenReturn400() throws Exception {
            RegisterRequest req = new RegisterRequest("ab", "valid@mail.com", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                      LOGIN                          ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("200 — valid login returns JWT token")
        void givenValidCredentials_whenLogin_thenReturnToken() throws Exception {
            // "testuser" with password "password123" was created in @BeforeEach
            LoginRequest req = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
        }

        @Test
        @DisplayName("401 — wrong password is rejected")
        void givenWrongPassword_whenLogin_thenReturn401() throws Exception {
            LoginRequest req = new LoginRequest(TEST_USERNAME, "wrongpassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 — non-existent user is rejected")
        void givenNonExistentUser_whenLogin_thenReturn401() throws Exception {
            LoginRequest req = new LoginRequest("ghostuser", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("400 — blank username triggers validation error")
        void givenBlankUsername_whenLogin_thenReturn400() throws Exception {
            LoginRequest req = new LoginRequest("", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    // ║                    PROFILE                          ║
    // ╚══════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("GET /api/users/me")
    class Profile {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("200 — authenticated user gets their profile")
        void givenAuthUser_whenGetProfile_thenReturn200() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.xp").value(0))
                    .andExpect(jsonPath("$.data.level").value(1))
                    .andExpect(jsonPath("$.data.coins").value(0))
                    .andExpect(jsonPath("$.data.roles").isArray());
        }

        @Test
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenGetProfile_thenReturn401() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
