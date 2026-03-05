package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    List<FocusSession> findByUserIdOrderByStartedAtDesc(Long userId);
    List<FocusSession> findByUserIdAndStartedAtBetween(Long userId, Instant start, Instant end);
}
