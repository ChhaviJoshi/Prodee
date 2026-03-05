package com.chhavi.prodee.productivity.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.productivity.dto.MilestoneRequest;
import com.chhavi.prodee.productivity.dto.MilestoneResponse;
import com.chhavi.prodee.productivity.entity.Milestone;
import com.chhavi.prodee.productivity.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;

    /**
     * Create a new milestone (countdown calendar).
     * startDate is always "today"; targetDate must be strictly in the future.
     */
    public MilestoneResponse createMilestone(String username, MilestoneRequest request) {
        User user = findUser(username);

        LocalDate startDate = LocalDate.now();
        if (!request.targetDate().isAfter(startDate)) {
            throw new BadRequestException("Target date must be strictly in the future.");
        }

        Milestone milestone = Milestone.builder()
                .user(user)
                .title(request.title())
                .startDate(startDate)
                .targetDate(request.targetDate())
                .build();

        return toResponse(milestoneRepository.save(milestone));
    }

    /**
     * Get all milestones for the authenticated user, sorted by target date.
     * Each response includes pre-calculated progress data and the boolean grid.
     */
    public List<MilestoneResponse> getMilestones(String username) {
        User user = findUser(username);
        return milestoneRepository.findByUserIdOrderByTargetDateAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get a single milestone by id (owned by the authenticated user).
     */
    public MilestoneResponse getMilestone(String username, Long id) {
        Milestone milestone = findMilestoneForUser(username, id);
        return toResponse(milestone);
    }

    /**
     * Delete a milestone owned by the authenticated user.
     */
    public void deleteMilestone(String username, Long id) {
        Milestone milestone = findMilestoneForUser(username, id);
        milestoneRepository.delete(milestone);
    }

    // ── Progress calculation ────────────────────────────────

    private MilestoneResponse toResponse(Milestone m) {
        long totalDays = ChronoUnit.DAYS.between(m.getStartDate(), m.getTargetDate());
        long daysPassed = ChronoUnit.DAYS.between(m.getStartDate(), LocalDate.now());

        // Clamp: daysPassed cannot exceed totalDays, and daysRemaining cannot drop below 0
        daysPassed = Math.max(0, Math.min(daysPassed, totalDays));
        long daysRemaining = totalDays - daysPassed;

        // Build the grid: first daysPassed elements are true (crossed out),
        // the rest are false.
        List<Boolean> grid = new ArrayList<>((int) totalDays);
        for (long i = 0; i < totalDays; i++) {
            grid.add(i < daysPassed);
        }

        return new MilestoneResponse(
                m.getId(),
                m.getTitle(),
                m.getStartDate(),
                m.getTargetDate(),
                totalDays,
                daysPassed,
                daysRemaining,
                grid
        );
    }

    // ── Helpers ─────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Milestone findMilestoneForUser(String username, Long milestoneId) {
        User user = findUser(username);
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));
        if (!milestone.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Milestone does not belong to you");
        }
        return milestone;
    }
}
