package com.chhavi.prodee.journaling.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A single pixel in the "Year in Pixels" grid.
 * References a LogTemplate and stores both the chosen intensity and its resolved hex color.
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

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;
}
