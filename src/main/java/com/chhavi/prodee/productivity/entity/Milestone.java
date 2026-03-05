package com.chhavi.prodee.productivity.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "milestones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    /** The day the user created the countdown tracker */
    @Column(nullable = false)
    private LocalDate startDate;

    /** The actual deadline */
    @Column(nullable = false)
    private LocalDate targetDate;

    @CreationTimestamp
    private Instant createdAt;
}
