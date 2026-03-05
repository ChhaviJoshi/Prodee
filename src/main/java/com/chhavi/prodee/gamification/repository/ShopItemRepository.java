package com.chhavi.prodee.gamification.repository;

import com.chhavi.prodee.gamification.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByLevelRequiredLessThanEqual(int level);
}
