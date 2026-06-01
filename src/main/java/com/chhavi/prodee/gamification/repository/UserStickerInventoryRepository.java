package com.chhavi.prodee.gamification.repository;

import com.chhavi.prodee.gamification.entity.UserStickerInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStickerInventoryRepository extends JpaRepository<UserStickerInventory, Long> {
    List<UserStickerInventory> findByUserId(Long userId);
    Optional<UserStickerInventory> findByUserIdAndStickerId(Long userId, Long stickerId);
}
