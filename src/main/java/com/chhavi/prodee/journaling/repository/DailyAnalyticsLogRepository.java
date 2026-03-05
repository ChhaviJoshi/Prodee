package com.chhavi.prodee.journaling.repository;

import com.chhavi.prodee.journaling.entity.DailyAnalyticsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAnalyticsLogRepository extends JpaRepository<DailyAnalyticsLog, Long> {
    List<DailyAnalyticsLog> findByUserIdOrderByLogDateDesc(Long userId);
    List<DailyAnalyticsLog> findByUserIdAndLogDateBetweenOrderByLogDateAsc(Long userId, LocalDate start, LocalDate end);
    Optional<DailyAnalyticsLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
