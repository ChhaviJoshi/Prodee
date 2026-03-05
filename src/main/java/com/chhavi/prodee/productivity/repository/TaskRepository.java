package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Task> findByUserIdAndCompleted(Long userId, boolean completed);
    List<Task> findByUserIdAndDueDate(Long userId, LocalDate dueDate);
    List<Task> findByUserIdAndIsPrivateFalse(Long userId);
    long countByUserIdAndCompletedTrue(Long userId);

    /** Get distinct non-null tags from incomplete tasks (for smart-feed aggregation) */
    @Query("SELECT DISTINCT t.tags FROM Task t WHERE t.completed = false AND t.tags IS NOT NULL")
    List<String> findAllDistinctActiveTaskTags();

    /** Get distinct non-null tags from a specific user's incomplete tasks */
    @Query("SELECT DISTINCT t.tags FROM Task t WHERE t.user.id = :userId AND t.completed = false AND t.tags IS NOT NULL")
    List<String> findDistinctActiveTagsByUserId(Long userId);
}
