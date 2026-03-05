package com.chhavi.prodee.gamification.event;

import com.chhavi.prodee.productivity.entity.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a user completes a task.
 * The GamificationService listens for this event to award XP & Coins.
 */
@Getter
public class TaskCompletedEvent extends ApplicationEvent {

    private final Task task;

    public TaskCompletedEvent(Object source, Task task) {
        super(source);
        this.task = task;
    }
}
