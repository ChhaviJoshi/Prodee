package com.chhavi.prodee.social.repository;

import com.chhavi.prodee.social.entity.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, Long> {
    Optional<Cohort> findByJoinCode(String joinCode);
}
