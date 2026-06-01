package com.chhavi.prodee.common.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.dto.NotificationResponse;
import com.chhavi.prodee.common.entity.SystemNotification;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.common.repository.SystemNotificationRepository;
import com.chhavi.prodee.productivity.entity.Milestone;
import com.chhavi.prodee.productivity.repository.HabitCompletionRepository;
import com.chhavi.prodee.productivity.repository.HabitRepository;
import com.chhavi.prodee.productivity.repository.MilestoneRepository;
import com.chhavi.prodee.productivity.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SystemNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final MilestoneRepository milestoneRepository;

    public List<NotificationResponse> getNotifications(String username) {
        User user = findUser(username);
        return notificationRepository.findTop30ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(String username) {
        User user = findUser(username);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public int markAllRead(String username) {
        User user = findUser(username);
        return notificationRepository.markAllReadByUserId(user.getId());
    }

    @Transactional
    public void createNotification(Long userId, String type, String message, String notificationKey) {
        if (notificationKey != null && notificationRepository.existsByNotificationKey(notificationKey)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        SystemNotification notification = SystemNotification.builder()
                .user(user)
                .type(type)
                .message(message)
                .notificationKey(notificationKey)
                .build();
        notificationRepository.save(notification);
    }

    @Scheduled(cron = "0 0 */5 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void pushIncompleteWorkReminders() {
        LocalDate today = LocalDate.now();
        int hourBucket = LocalDateTime.now().getHour() / 5;

        for (User user : userRepository.findAll()) {
            long openTasks = taskRepository.findByUserIdAndCompleted(user.getId(), false).size();
            long dailyHabits = habitRepository.countActiveDailyHabitsByUserId(user.getId());
            long completedDailyHabits = habitCompletionRepository.countDailyCompletionsByUserIdAndDate(user.getId(), today);
            long pendingHabits = Math.max(0, dailyHabits - completedDailyHabits);

            if (openTasks == 0 && pendingHabits == 0) {
                continue;
            }

            String message = "You still have " + openTasks + " open task(s) and " + pendingHabits
                    + " pending daily habit(s). Finish strong today.";
            String key = "REMINDER:" + user.getId() + ":" + today + ":" + hourBucket;
            createNotification(user.getId(), "REMINDER", message, key);
        }
    }

    @Scheduled(cron = "0 15 * * * *", zone = "Asia/Kolkata")
    @Transactional
    public void pushMilestoneProgressReminders() {
        for (Milestone milestone : milestoneRepository.findAll()) {
            long totalDays = ChronoUnit.DAYS.between(milestone.getStartDate(), milestone.getTargetDate());
            if (totalDays <= 0) {
                continue;
            }

            long daysPassed = ChronoUnit.DAYS.between(milestone.getStartDate(), LocalDate.now());
            daysPassed = Math.max(0, Math.min(daysPassed, totalDays));
            int pct = (int) Math.floor((daysPassed * 100.0) / totalDays);

            if (pct >= 75) {
                String key = "MILESTONE:" + milestone.getId() + ":75";
                String message = "Milestone '" + milestone.getTitle() + "' is 75% complete. Final stretch.";
                createNotification(milestone.getUser().getId(), "MILESTONE", message, key);
            } else if (pct >= 50) {
                String key = "MILESTONE:" + milestone.getId() + ":50";
                String message = "Milestone '" + milestone.getTitle() + "' has crossed 50% progress.";
                createNotification(milestone.getUser().getId(), "MILESTONE", message, key);
            }
        }

        log.debug("Milestone reminder scheduler executed");
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private NotificationResponse toResponse(SystemNotification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
