package com.chhavi.prodee.gamification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stickers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int price;
}
