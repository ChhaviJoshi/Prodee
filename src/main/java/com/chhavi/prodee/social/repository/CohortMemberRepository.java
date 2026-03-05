package com.chhavi.prodee.social.repository;

import com.chhavi.prodee.social.entity.CohortMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CohortMemberRepository extends JpaRepository<CohortMember, Long> {
    List<CohortMember> findByCohortIdOrderByDailyScoreDesc(Long cohortId);
    Optional<CohortMember> findByCohortIdAndUserId(Long cohortId, Long userId);
    List<CohortMember> findByUserId(Long userId);
    boolean existsByCohortIdAndUserId(Long cohortId, Long userId);

    /** Bulk-reset all daily scores to zero at midnight */
    @Modifying
    @Query("UPDATE CohortMember cm SET cm.dailyScore = 0")
    int resetAllDailyScores();
}
