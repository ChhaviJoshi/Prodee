package com.chhavi.prodee.gamification.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_sticker_inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sticker_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserStickerInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Builder.Default
    private int quantity = 1;

    @CreationTimestamp
    private Instant acquiredAt;
}
