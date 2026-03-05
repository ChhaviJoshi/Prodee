package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    Optional<HabitCompletion> findByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    boolean existsByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    List<HabitCompletion> findByUserIdAndCompletedDateBetweenOrderByCompletedDateAsc(
            Long userId, LocalDate start, LocalDate end);

    long countByHabitId(Long habitId);
}
