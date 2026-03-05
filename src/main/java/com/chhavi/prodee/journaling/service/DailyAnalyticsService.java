package com.chhavi.prodee.journaling.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.journaling.dto.DailyAnalyticsRequest;
import com.chhavi.prodee.journaling.dto.DailyAnalyticsResponse;
import com.chhavi.prodee.journaling.entity.DailyAnalyticsLog;
import com.chhavi.prodee.journaling.repository.DailyAnalyticsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyAnalyticsService {

    private final DailyAnalyticsLogRepository analyticsLogRepository;
    private final UserRepository userRepository;

    /**
     * Log or update daily analytics. Enforces one entry per user per day.
     * If an entry already exists for that date, it is updated.
     */
    @Transactional
    public DailyAnalyticsResponse logAnalytics(String username, DailyAnalyticsRequest request) {
        User user = findUser(username);

        Optional<DailyAnalyticsLog> existing = analyticsLogRepository
                .findByUserIdAndLogDate(user.getId(), request.date());

        DailyAnalyticsLog log;
        if (existing.isPresent()) {
            // Update existing log for this date
            log = existing.get();
            log.setSleepHours(request.sleepHours());
            log.setScreenTimeMinutes(request.screenTimeMinutes());
            log.setProductivityFocusMinutes(request.productivityFocusMinutes());
        } else {
            log = DailyAnalyticsLog.builder()
                    .user(user)
                    .logDate(request.date())
                    .sleepHours(request.sleepHours())
                    .screenTimeMinutes(request.screenTimeMinutes())
                    .productivityFocusMinutes(request.productivityFocusMinutes())
                    .build();
        }
        return toResponse(analyticsLogRepository.save(log));
    }

    public List<DailyAnalyticsResponse> getLogs(String username) {
        User user = findUser(username);
        return analyticsLogRepository.findByUserIdOrderByLogDateDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns the last 7 days of analytics data, formatted for Recharts / Chart.js.
     * Each entry includes date, sleepHours, screenTimeMinutes, productivityFocusMinutes.
     */
    public List<DailyAnalyticsResponse> getWeeklyLogs(String username) {
        User user = findUser(username);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        return analyticsLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateAsc(user.getId(), start, end)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns the last 30 days of analytics data, formatted for Recharts / Chart.js.
     */
    public List<DailyAnalyticsResponse> getMonthlyLogs(String username) {
        User user = findUser(username);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        return analyticsLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateAsc(user.getId(), start, end)
                .stream().map(this::toResponse).toList();
    }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private DailyAnalyticsResponse toResponse(DailyAnalyticsLog h) {
        return new DailyAnalyticsResponse(
                h.getId(), h.getLogDate(), h.getSleepHours(),
                h.getScreenTimeMinutes(), h.getProductivityFocusMinutes());
    }
}
