package com.chhavi.prodee.social.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cohorts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Unique 8-character join code */
    @Column(unique = true, nullable = false, length = 8)
    private String joinCode;

    @OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CohortMember> members = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;
}
