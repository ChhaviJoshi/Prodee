package com.chhavi.prodee.productivity.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "focus_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Expected focus duration in minutes */
    private int expectedDurationMinutes;

    /** Actual focus duration in minutes */
    private int actualDurationMinutes;

    /** Calculated efficiency: (actual / expected) * 100, capped at 100 */
    private double efficiencyScore;

    /** e.g. "RAIN", "FIREPLACE", "LOFI", "SILENCE" */
    private String ambientType;

    private Instant startedAt;
    private Instant endedAt;
}
