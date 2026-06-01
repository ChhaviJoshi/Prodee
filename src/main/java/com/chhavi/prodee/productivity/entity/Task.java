package com.chhavi.prodee.productivity.entity;

import com.chhavi.prodee.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskDifficulty difficulty;

    @Builder.Default
    private boolean completed = false;

    /** Comma-separated tags e.g. "Java,Learning,Backend" */
    private String tags;

    // Backward-compatible with existing DB schema where `recurring` may still be NOT NULL.
    @Builder.Default
    @Column(nullable = false)
    private boolean recurring = false;

    private LocalDate dueDate;
    private Instant completedAt;

    @CreationTimestamp
    private Instant createdAt;
}
