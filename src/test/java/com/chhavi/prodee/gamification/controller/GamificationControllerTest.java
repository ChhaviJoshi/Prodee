package com.chhavi.prodee.gamification.controller;

import com.chhavi.prodee.AbstractIntegrationTest;
import com.chhavi.prodee.gamification.entity.ItemCategory;
import com.chhavi.prodee.gamification.entity.ShopItem;
import com.chhavi.prodee.gamification.repository.ShopItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code /api/gamification}.
 * Covers XP/level status, shop listing, purchasing, inventory,
 * and insufficient-funds / level-lock scenarios.
 */
@WithMockUser(username = "testuser", roles = "USER")
@DisplayName("GamificationController Integration Tests")
class GamificationControllerTest extends AbstractIntegrationTest {

    private static final String BASE = "/api/gamification";

    @Autowired
    private ShopItemRepository shopItemRepository;

    private ShopItem cheapItem;
    private ShopItem expensiveItem;

    @BeforeEach
    void seedShop() {
        cheapItem = shopItemRepository.save(ShopItem.builder()
                .name("Test Sword")
                .description("A wooden sword")
                .category(ItemCategory.AVATAR_PROP)
                .price(10)
                .levelRequired(1)
                .build());

        expensiveItem = shopItemRepository.save(ShopItem.builder()
                .name("Dragon Armor")
                .description("Legendary armor")
                .category(ItemCategory.AVATAR_PROP)
                .price(500)
                .levelRequired(10)
                .build());
    }

    // ── STATUS ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/gamification/status")
    @WithMockUser(username = "testuser", roles = "USER")
    class Status {

        @Test
        @DisplayName("200 — returns XP, level, coins for new user")
        void givenNewUser_whenGetStatus_thenReturn200() throws Exception {
            mockMvc.perform(get(BASE + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.xp").value(0))
                    .andExpect(jsonPath("$.data.level").value(1))
                    .andExpect(jsonPath("$.data.coins").value(0))
                    .andExpect(jsonPath("$.data.xpToNextLevel").isNumber());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("401 — unauthenticated request is rejected")
        void givenNoAuth_whenGetStatus_thenReturn401() throws Exception {
            mockMvc.perform(get(BASE + "/status"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── SHOP ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/gamification/shop")
    @WithMockUser(username = "testuser", roles = "USER")
    class Shop {

        @Test
        @DisplayName("200 — returns all shop items")
        void givenShopItems_whenGetAll_thenReturnList() throws Exception {
            mockMvc.perform(get(BASE + "/shop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath("$.data[0].name").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].price").isNumber());
        }

        @Test
        @DisplayName("200 — available items filtered by user level")
        void givenLevel1User_whenGetAvailable_thenFilterByLevel() throws Exception {
            mockMvc.perform(get(BASE + "/shop/available"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
            // Level 1 user sees the cheap item but not the level-10 item
        }
    }

    // ── BUY ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/gamification/shop/buy/{itemId}")
    @WithMockUser(username = "testuser", roles = "USER")
    class Buy {

        @Test
        @DisplayName("200 — purchasing item with enough coins succeeds")
        void givenEnoughCoins_whenBuy_thenReturn200() throws Exception {
            // Give the test user enough coins
            testUser.setCoins(100);
            userRepository.save(testUser);

            mockMvc.perform(post(BASE + "/shop/buy/" + cheapItem.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("Test Sword"))
                    .andExpect(jsonPath("$.data.quantity").value(1));
        }

        @Test
        @DisplayName("400 — purchasing item without enough coins fails")
        void givenNotEnoughCoins_whenBuy_thenReturn400() throws Exception {
            // testUser starts with 0 coins
            mockMvc.perform(post(BASE + "/shop/buy/" + cheapItem.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — purchasing level-locked item fails")
        void givenLowLevel_whenBuyHighLevelItem_thenReturn400() throws Exception {
            testUser.setCoins(1000);
            userRepository.save(testUser);

            mockMvc.perform(post(BASE + "/shop/buy/" + expensiveItem.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("404 — purchasing non-existent item fails")
        void givenNonExistentItem_whenBuy_thenReturn404() throws Exception {
            testUser.setCoins(1000);
            userRepository.save(testUser);

            mockMvc.perform(post(BASE + "/shop/buy/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── INVENTORY ───────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/gamification/inventory")
    @WithMockUser(username = "testuser", roles = "USER")
    class Inventory {

        @Test
        @DisplayName("200 — empty inventory for new user")
        void givenNoPurchases_whenGetInventory_thenReturnEmpty() throws Exception {
            mockMvc.perform(get(BASE + "/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("200 — inventory contains purchased item")
        void givenPurchase_whenGetInventory_thenReturnItem() throws Exception {
            testUser.setCoins(100);
            userRepository.save(testUser);

            // Buy an item first
            mockMvc.perform(post(BASE + "/shop/buy/" + cheapItem.getId()))
                    .andExpect(status().isOk());

            mockMvc.perform(get(BASE + "/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].itemName").value("Test Sword"));
        }
    }
}
