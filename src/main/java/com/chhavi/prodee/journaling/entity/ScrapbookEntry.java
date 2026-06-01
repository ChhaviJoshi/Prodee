package com.chhavi.prodee.journaling.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Rich-text scrapbook / diary entry. Images stored on Cloudinary.
 */
@Entity
@Table(name = "scrapbook_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScrapbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** Cloudinary URL of attached image */
    private String imageUrl;

    /** JSON array of placed stickers: [{"stickerId":1,"x":24,"y":40}] */
    @Column(columnDefinition = "TEXT")
    private String placedStickersJson;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
