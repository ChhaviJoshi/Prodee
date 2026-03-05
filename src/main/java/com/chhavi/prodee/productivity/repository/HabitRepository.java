package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdAndActiveTrue(Long userId);
    List<Habit> findByUserId(Long userId);

    @Query("SELECT DISTINCT h.tag FROM Habit h WHERE h.active = true AND h.tag IS NOT NULL")
    List<String> findAllDistinctActiveTags();

    /** Get distinct tags for a specific user's active habits */
    @Query("SELECT DISTINCT h.tag FROM Habit h WHERE h.user.id = :userId AND h.active = true AND h.tag IS NOT NULL")
    List<String> findDistinctActiveTagsByUserId(Long userId);

    /**
     * Bulk reset streaks for DAILY habits that were NOT completed yesterday.
     * Uses a NOT EXISTS sub-query against habit_completions to avoid N+1.
     */
    @Modifying
    @Query("""
        UPDATE Habit h SET h.streak = 0
        WHERE h.active = true
          AND UPPER(h.frequency) = 'DAILY'
          AND NOT EXISTS (
              SELECT 1 FROM HabitCompletion hc
              WHERE hc.habit.id = h.id AND hc.completedDate = :yesterday
          )
    """)
    int resetStaleStreaks(LocalDate yesterday);
}
