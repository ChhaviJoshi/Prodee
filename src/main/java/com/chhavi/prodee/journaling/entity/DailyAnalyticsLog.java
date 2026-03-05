package com.chhavi.prodee.journaling.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Quantitative daily analytics log for charts and AI insights.
 * Strictly one entry per user per day.
 */
@Entity
@Table(name = "daily_analytics_logs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyAnalyticsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    /** Sleep duration in hours */
    @Column(nullable = false)
    private Double sleepHours;

    /** Digital wellbeing: screen time in minutes */
    @Column(nullable = false)
    private Integer screenTimeMinutes;

    /** Deep work / productive focus time in minutes */
    @Column(nullable = false)
    private Integer productivityFocusMinutes;
}
