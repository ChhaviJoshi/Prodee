package com.chhavi.prodee.productivity.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.gamification.event.HabitCompletedEvent;
import com.chhavi.prodee.productivity.dto.HabitRequest;
import com.chhavi.prodee.productivity.dto.HabitResponse;
import com.chhavi.prodee.productivity.entity.Habit;
import com.chhavi.prodee.productivity.entity.HabitCompletion;
import com.chhavi.prodee.productivity.repository.HabitCompletionRepository;
import com.chhavi.prodee.productivity.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public HabitResponse createHabit(String username, HabitRequest request) {
        User user = findUser(username);
        Habit habit = Habit.builder()
                .user(user)
                .title(request.title())
                .tag(request.tag())
                .frequency(request.frequency().toUpperCase())
                .build();
        return toResponse(habitRepository.save(habit));
    }

    public List<HabitResponse> getUserHabits(String username) {
        User user = findUser(username);
        List<Habit> habits = habitRepository.findByUserId(user.getId());
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        for (Habit habit : habits) {
            if ("DAILY".equalsIgnoreCase(habit.getFrequency()) && habit.getStreak() > 0) {
                boolean completedTodayOrYesterday = completionRepository.existsByHabitIdAndCompletedDate(habit.getId(), today) ||
                                                    completionRepository.existsByHabitIdAndCompletedDate(habit.getId(), yesterday);
                if (!completedTodayOrYesterday) {
                    habit.setStreak(0);
                    habitRepository.save(habit);
                }
            }
        }
        return habits.stream().map(this::toResponse).toList();
    }

    /**
     * Complete a habit for today.
     * - Prevents double-completion on the same day (unique constraint on habit_id + completed_date).
     * - If yesterday was also completed → streak++; otherwise streak resets to 1.
     * - Fires a HabitCompletedEvent for gamification XP/coin rewards.
     */
    @Transactional
    public HabitResponse completeHabit(String username, Long habitId) {
        Habit habit = findHabitForUser(username, habitId);
        LocalDate today = LocalDate.now();

        if (completionRepository.existsByHabitIdAndCompletedDate(habitId, today)) {
            throw new BadRequestException("Habit already completed for today");
        }

        // Check if yesterday was completed → continue streak or restart
        boolean yesterdayCompleted = completionRepository
                .existsByHabitIdAndCompletedDate(habitId, today.minusDays(1));

        if (yesterdayCompleted) {
            habit.setStreak(habit.getStreak() + 1);
        } else {
            habit.setStreak(1);
        }
        habitRepository.save(habit);

        // Record the completion
        HabitCompletion completion = HabitCompletion.builder()
                .habit(habit)
                .user(habit.getUser())
                .completedDate(today)
                .build();
        completionRepository.save(completion);

        // Fire event for gamification
        eventPublisher.publishEvent(new HabitCompletedEvent(this, habit, habit.getStreak()));

        log.info("Habit '{}' completed by {} — streak now {}", habit.getTitle(),
                username, habit.getStreak());

        return toResponse(habit);
    }

    @Transactional
    @Deprecated // Use completeHabit() instead — kept for backward compatibility
    public HabitResponse incrementStreak(String username, Long habitId) {
        return completeHabit(username, habitId);
    }

    @Transactional
    public HabitResponse updateHabit(String username, Long habitId, HabitRequest request) {
        Habit habit = findHabitForUser(username, habitId);
        habit.setTitle(request.title());
        habit.setTag(request.tag());
        habit.setFrequency(request.frequency().toUpperCase());
        return toResponse(habitRepository.save(habit));
    }

    @Transactional
    public void deleteHabit(String username, Long habitId) {
        Habit habit = findHabitForUser(username, habitId);
        habitRepository.delete(habit);
    }

    // ── Scheduled: bulk‑reset stale streaks ─────────────────

    /**
     * CRON: 00:05 AM IST daily.
     * Resets streaks for DAILY habits that were NOT completed yesterday.
     * Uses a single bulk JPQL UPDATE — no N+1.
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void resetStaleStreaks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int reset = habitRepository.resetStaleStreaks(yesterday);
        log.info("Reset {} stale daily habit streaks (missed {})", reset, yesterday);
    }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Habit findHabitForUser(String username, Long habitId) {
        User user = findUser(username);
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", "id", habitId));
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Habit does not belong to the current user");
        }
        return habit;
    }

    private HabitResponse toResponse(Habit h) {
        return new HabitResponse(
                h.getId(), h.getTitle(), h.getTag(), h.getFrequency(),
                h.getStreak(), h.isActive(), h.getCreatedAt());
    }
}
