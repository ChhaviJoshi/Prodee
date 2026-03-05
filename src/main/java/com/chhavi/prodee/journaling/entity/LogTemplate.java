package com.chhavi.prodee.journaling.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Each user can define their own "Year in Pixels" templates (EAV pattern).
 * e.g. "Mood", "Sleep Quality", "Dream Quality" → each with custom color mappings.
 */
@Entity
@Table(name = "log_templates",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Template name, e.g. "Mood", "Anxiety Level" */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * JSON map of intensity→color, e.g.
     * {"1":"#1a1a1a","2":"#ff4500","3":"#ffd700","4":"#32cd32"}
     * Keys must be integer strings; values must be hex color codes.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String colorMapping;

    @CreationTimestamp
    private Instant createdAt;
}
