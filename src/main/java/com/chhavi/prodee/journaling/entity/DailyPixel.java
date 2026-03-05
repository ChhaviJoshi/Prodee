package com.chhavi.prodee.journaling.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A single pixel in the "Year in Pixels" grid.
 * References a LogTemplate and stores the intensity level for the day.
 * The frontend uses intensity to look up the hex color from the template's colorMapping.
 */
@Entity
@Table(name = "daily_pixels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "template_id", "pixel_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyPixel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    private LogTemplate template;

    @Column(name = "pixel_date", nullable = false)
    private LocalDate pixelDate;

    /** Intensity level (integer key into the template's colorMapping JSON) */
    @Column(nullable = false)
    private Integer intensity;
}
