package com.chhavi.prodee.gamification.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.gamification.dto.GamificationStatus;
import com.chhavi.prodee.gamification.dto.InventoryItemResponse;
import com.chhavi.prodee.gamification.dto.ShopItemResponse;
import com.chhavi.prodee.gamification.service.GamificationService;
import com.chhavi.prodee.gamification.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamification", description = "XP, Levels, Coins, Shop, Inventory")
public class GamificationController {

    private final GamificationService gamificationService;
    private final ShopService shopService;

    @GetMapping("/status")
    @Operation(summary = "Get current XP, level, coins, and XP needed for next level")
    public ResponseEntity<ApiResponse<GamificationStatus>> getStatus(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(gamificationService.getStatus(user.getUsername())));
    }

    @GetMapping("/shop")
    @Operation(summary = "Get all shop items")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getAllShopItems() {
        return ResponseEntity.ok(ApiResponse.success(shopService.getAllItems()));
    }

    @GetMapping("/shop/available")
    @Operation(summary = "Get shop items available at your current level")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getAvailable(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getAvailableItems(user.getUsername())));
    }

    @PostMapping("/shop/buy/{itemId}")
    @Operation(summary = "Purchase an item from the shop")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> buyItem(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long itemId) {
        InventoryItemResponse item = shopService.purchaseItem(user.getUsername(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item purchased!", item));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Get the current user's inventory")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventory(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getInventory(user.getUsername())));
    }
}
