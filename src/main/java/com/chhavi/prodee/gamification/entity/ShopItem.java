package com.chhavi.prodee.gamification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Column(nullable = false)
    private int price;

    /** URL to the pixel art sprite/icon */
    private String imageUrl;

    /** Minimum level required to purchase */
    @Builder.Default
    private int levelRequired = 1;
}
