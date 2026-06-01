package com.chhavi.prodee.gamification.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.gamification.dto.InventoryItemResponse;
import com.chhavi.prodee.gamification.dto.ShopItemResponse;
import com.chhavi.prodee.gamification.dto.StickerInventoryResponse;
import com.chhavi.prodee.gamification.dto.StickerResponse;
import com.chhavi.prodee.gamification.entity.ShopItem;
import com.chhavi.prodee.gamification.entity.Sticker;
import com.chhavi.prodee.gamification.entity.UserInventory;
import com.chhavi.prodee.gamification.entity.UserStickerInventory;
import com.chhavi.prodee.gamification.repository.ShopItemRepository;
import com.chhavi.prodee.gamification.repository.StickerRepository;
import com.chhavi.prodee.gamification.repository.UserInventoryRepository;
import com.chhavi.prodee.gamification.repository.UserStickerInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final StickerRepository stickerRepository;
    private final UserInventoryRepository inventoryRepository;
    private final UserStickerInventoryRepository stickerInventoryRepository;
    private final UserRepository userRepository;

    public List<ShopItemResponse> getAllItems() {
        return shopItemRepository.findAll().stream().map(this::toShopResponse).toList();
    }

    public List<ShopItemResponse> getAvailableItems(String username) {
        User user = findUser(username);
        return shopItemRepository.findByLevelRequiredLessThanEqual(user.getLevel())
                .stream().map(this::toShopResponse).toList();
    }

    @Transactional
    public InventoryItemResponse purchaseItem(String username, Long itemId) {
        User user = findUser(username);
        ShopItem item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ShopItem", "id", itemId));

        if (user.getLevel() < item.getLevelRequired()) {
            throw new BadRequestException("You need to be Level " + item.getLevelRequired() + " to buy this item.");
        }
        if (user.getCoins() < item.getPrice()) {
            throw new BadRequestException("Not enough coins! You need " + item.getPrice() + " but have " + user.getCoins());
        }

        user.setCoins(user.getCoins() - item.getPrice());
        userRepository.save(user);

        // Check if user already owns this item (stack quantity)
        var existing = inventoryRepository.findByUserIdAndItemId(user.getId(), itemId);
        UserInventory inv;
        if (existing.isPresent()) {
            inv = existing.get();
            inv.setQuantity(inv.getQuantity() + 1);
        } else {
            inv = UserInventory.builder()
                    .user(user)
                    .item(item)
                    .quantity(1)
                    .build();
        }
        inventoryRepository.save(inv);

        return toInventoryResponse(inv);
    }

    public List<InventoryItemResponse> getInventory(String username) {
        User user = findUser(username);
        return inventoryRepository.findByUserId(user.getId())
                .stream().map(this::toInventoryResponse).toList();
    }

    public List<StickerResponse> getAllStickers() {
        return stickerRepository.findAll().stream().map(this::toStickerResponse).toList();
    }

    @Transactional
    public StickerInventoryResponse purchaseSticker(String username, Long stickerId) {
        User user = findUser(username);
        Sticker sticker = stickerRepository.findById(stickerId)
                .orElseThrow(() -> new ResourceNotFoundException("Sticker", "id", stickerId));

        if (user.getCoins() < sticker.getPrice()) {
            throw new BadRequestException("Not enough coins! You need " + sticker.getPrice() + " but have " + user.getCoins());
        }

        user.setCoins(user.getCoins() - sticker.getPrice());
        userRepository.save(user);

        var existing = stickerInventoryRepository.findByUserIdAndStickerId(user.getId(), stickerId);
        UserStickerInventory inventory;
        if (existing.isPresent()) {
            inventory = existing.get();
            inventory.setQuantity(inventory.getQuantity() + 1);
        } else {
            inventory = UserStickerInventory.builder()
                    .user(user)
                    .sticker(sticker)
                    .quantity(1)
                    .build();
        }

        return toStickerInventoryResponse(stickerInventoryRepository.save(inventory));
    }

    public List<StickerInventoryResponse> getStickerInventory(String username) {
        User user = findUser(username);
        return stickerInventoryRepository.findByUserId(user.getId())
                .stream().map(this::toStickerInventoryResponse).toList();
    }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private ShopItemResponse toShopResponse(ShopItem item) {
        return new ShopItemResponse(
                item.getId(), item.getName(), item.getDescription(),
                item.getCategory(), item.getPrice(), item.getImageUrl(), item.getLevelRequired());
    }

    private InventoryItemResponse toInventoryResponse(UserInventory inv) {
        return new InventoryItemResponse(
                inv.getId(), inv.getItem().getId(), inv.getItem().getName(),
                inv.getItem().getCategory().name(), inv.getQuantity(), inv.getAcquiredAt());
    }

    private StickerResponse toStickerResponse(Sticker sticker) {
        return new StickerResponse(
                sticker.getId(), sticker.getName(), sticker.getImageUrl(), sticker.getPrice());
    }

    private StickerInventoryResponse toStickerInventoryResponse(UserStickerInventory inventory) {
        return new StickerInventoryResponse(
                inventory.getId(),
                inventory.getSticker().getId(),
                inventory.getSticker().getName(),
                inventory.getSticker().getImageUrl(),
                inventory.getQuantity(),
                inventory.getAcquiredAt());
    }
}
