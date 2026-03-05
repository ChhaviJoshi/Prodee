package com.chhavi.prodee.productivity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "aggregated_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AggregatedArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String url;

    private String source;

    /** Comma-separated tags this article matches, e.g. "java,spring" */
    private String tags;

    private String coverImageUrl;

    @CreationTimestamp
    private Instant fetchedAt;

    /** Append a tag if not already present in the comma-separated list */
    public void addTag(String newTag) {
        if (this.tags == null || this.tags.isBlank()) {
            this.tags = newTag.toLowerCase();
            return;
        }
        String lower = newTag.toLowerCase();
        for (String existing : this.tags.split(",")) {
            if (existing.trim().equalsIgnoreCase(lower)) return;
        }
        this.tags = this.tags + "," + lower;
    }
}
