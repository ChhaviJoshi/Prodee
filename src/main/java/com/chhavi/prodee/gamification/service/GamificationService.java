package com.chhavi.prodee.gamification.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.gamification.dto.GamificationStatus;
import com.chhavi.prodee.gamification.event.HabitCompletedEvent;
import com.chhavi.prodee.gamification.event.TaskCompletedEvent;
import com.chhavi.prodee.productivity.entity.Task;
import com.chhavi.prodee.productivity.entity.TaskDifficulty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserRepository userRepository;

    /** XP required to reach the next level: level * 100 */
    private static int xpForLevel(int level) {
        return level * 100;
    }

    /** Base XP/coin reward for completing a habit */
    private static final int HABIT_XP_REWARD = 5;
    private static final int HABIT_COIN_REWARD = 3;

    @EventListener
    @Transactional
    public void onTaskCompleted(TaskCompletedEvent event) {
        Task task = event.getTask();
        User user = task.getUser();
        TaskDifficulty diff = task.getDifficulty();

        awardXpAndCoins(user, diff.getXpReward(), diff.getCoinReward());

        log.info("Awarded {} XP and {} Coins to {} for task (now Level {})",
                diff.getXpReward(), diff.getCoinReward(), user.getUsername(), user.getLevel());
    }

    @EventListener
    @Transactional
    public void onHabitCompleted(HabitCompletedEvent event) {
        User user = event.getHabit().getUser();
        int streak = event.getCurrentStreak();

        int xp = HABIT_XP_REWARD;
        int coins = HABIT_COIN_REWARD;

        // Streak bonuses
        if (streak == 7)  { xp += 20;  coins += 10; log.info("🔥 7-day streak bonus for {}!", user.getUsername()); }
        if (streak == 30) { xp += 50;  coins += 30; log.info("🔥 30-day streak bonus for {}!", user.getUsername()); }
        if (streak == 100){ xp += 100; coins += 60; log.info("🔥 100-day streak bonus for {}!", user.getUsername()); }

        awardXpAndCoins(user, xp, coins);

        log.info("Awarded {} XP and {} Coins to {} for habit (streak {}, Level {})",
                xp, coins, user.getUsername(), streak, user.getLevel());
    }

    public GamificationStatus getStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        int xpToNext = xpForLevel(user.getLevel() + 1) - user.getXp();
        return new GamificationStatus(
                user.getId(), user.getUsername(),
                user.getXp(), user.getLevel(), user.getCoins(),
                Math.max(0, xpToNext));
    }

    // ── helpers ──────────────────────────────────────────────

    private void awardXpAndCoins(User user, int xp, int coins) {
        user.setXp(user.getXp() + xp);
        user.setCoins(user.getCoins() + coins);

        while (user.getXp() >= xpForLevel(user.getLevel() + 1)) {
            user.setLevel(user.getLevel() + 1);
            log.info("🎉 {} leveled up to Level {}!", user.getUsername(), user.getLevel());
        }

        userRepository.save(user);
    }
}
