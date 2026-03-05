package com.chhavi.prodee;

import com.chhavi.prodee.auth.entity.ERole;
import com.chhavi.prodee.auth.entity.Role;
import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.RoleRepository;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Base integration test class.
 * <p>
 * ● Boots the full Spring context with H2 in-memory (profile "test").
 * ● Every test method runs inside a transaction that auto-rolls-back,
 *   keeping the database perfectly clean between tests.
 * ● Seeds ROLE_USER / ROLE_ADMIN and creates a default {@code testUser}
 *   that matches the {@code @WithMockUser(username = "testuser")} annotation
 *   used on concrete test classes.
 * </p>
 *
 * <h3>Authentication Strategy</h3>
 * All secured test classes use Spring Security Test's
 * {@code @WithMockUser(username = "testuser", roles = "USER")} at class level.
 * This injects a mock {@code UserDetails} whose {@code getUsername()} returns
 * "testuser". Because {@code @BeforeEach} inserts a real {@code User} row with
 * that username, the services' {@code userRepository.findByUsername("testuser")}
 * calls resolve successfully — no real JWT is needed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    /* ── Shared test infrastructure ──────────────────────── */

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /* ── Constants ───────────────────────────────────────── */

    protected static final String TEST_USERNAME = "testuser";
    protected static final String TEST_EMAIL    = "testuser@example.com";
    protected static final String TEST_PASSWORD = "password123";

    protected static final String OTHER_USERNAME = "otheruser";
    protected static final String OTHER_EMAIL    = "other@example.com";

    /* ── State ───────────────────────────────────────────── */

    protected User testUser;
    protected Role roleUser;
    protected Role roleAdmin;

    /* ── Setup ───────────────────────────────────────────── */

    @BeforeEach
    void setUpBase() {
        // Seed roles (safe inside the rolled-back transaction)
        roleUser = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ROLE_USER).build()));
        roleAdmin = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build()));

        // Create the primary test user (matches @WithMockUser username)
        testUser = userRepository.save(User.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roles(Set.of(roleUser))
                .build());
    }

    /* ── Helpers ─────────────────────────────────────────── */

    /** Create a second user for ownership / isolation tests. */
    protected User createOtherUser() {
        return userRepository.save(User.builder()
                .username(OTHER_USERNAME)
                .email(OTHER_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roles(Set.of(roleUser))
                .build());
    }

    /** Serialize any object to JSON. */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
