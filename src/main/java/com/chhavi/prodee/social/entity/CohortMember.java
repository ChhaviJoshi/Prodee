package com.chhavi.prodee.social.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cohort_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cohort_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CohortMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CohortRole role;

    /** Daily score accumulated from completing tasks */
    @Builder.Default
    private int dailyScore = 0;

    /** Number of days this member finished rank #1 (ties included). */
    @Builder.Default
    private int firstPlaceFinishes = 0;

    @CreationTimestamp
    private Instant joinedAt;
}
