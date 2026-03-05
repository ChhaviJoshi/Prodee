package com.chhavi.prodee.gamification.event;

import com.chhavi.prodee.productivity.entity.Habit;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a user completes a daily habit.
 * The GamificationService listens for this event to award XP & Coins.
 */
@Getter
public class HabitCompletedEvent extends ApplicationEvent {

    private final Habit habit;
    private final int currentStreak;

    public HabitCompletedEvent(Object source, Habit habit, int currentStreak) {
        super(source);
        this.habit = habit;
        this.currentStreak = currentStreak;
    }
}
