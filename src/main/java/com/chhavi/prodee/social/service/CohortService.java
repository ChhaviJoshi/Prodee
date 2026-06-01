package com.chhavi.prodee.social.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.social.dto.*;
import com.chhavi.prodee.social.entity.Cohort;
import com.chhavi.prodee.social.entity.CohortMember;
import com.chhavi.prodee.social.entity.CohortRole;
import com.chhavi.prodee.productivity.repository.HabitCompletionRepository;
import com.chhavi.prodee.productivity.repository.TaskRepository;
import com.chhavi.prodee.social.repository.CohortMemberRepository;
import com.chhavi.prodee.social.repository.CohortRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CohortService {

    private final CohortRepository cohortRepository;
    private final CohortMemberRepository memberRepository;
    private final UserRepository userRepository;
        private final TaskRepository taskRepository;
        private final HabitCompletionRepository habitCompletionRepository;

        private static final int TASK_WEEKLY_SCORE = 10;
        private static final int HABIT_WEEKLY_SCORE = 5;
        private static final int DAILY_WIN_XP_REWARD = 40;
        private static final int DAILY_WIN_COIN_REWARD = 25;

    @Transactional
    public CohortResponse createCohort(String username, CreateCohortRequest request) {
        User user = findUser(username);
        String joinCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Cohort cohort = Cohort.builder()
                .name(request.name())
                .joinCode(joinCode)
                .build();
        cohort = cohortRepository.save(cohort);

        CohortMember admin = CohortMember.builder()
                .cohort(cohort)
                .user(user)
                .role(CohortRole.ADMIN)
                .build();
        memberRepository.save(admin);

        return toCohortResponse(cohort);
    }

    @Transactional
    public CohortResponse joinCohort(String username, String joinCode) {
        User user = findUser(username);
        Cohort cohort = cohortRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort", "joinCode", joinCode));

        if (memberRepository.existsByCohortIdAndUserId(cohort.getId(), user.getId())) {
            throw new BadRequestException("You are already a member of this cohort");
        }

        CohortMember member = CohortMember.builder()
                .cohort(cohort)
                .user(user)
                .role(CohortRole.MEMBER)
                .build();
        memberRepository.save(member);

        return toCohortResponse(cohort);
    }

    public CohortResponse getCohort(Long cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort", "id", cohortId));
        return toCohortResponse(cohort);
    }

    public List<CohortResponse> getUserCohorts(String username) {
        User user = findUser(username);
        return memberRepository.findByUserId(user.getId()).stream()
                .map(m -> toCohortResponse(m.getCohort()))
                .toList();
    }

    public List<LeaderboardEntry> getLeaderboard(Long cohortId) {
        List<CohortMember> members = memberRepository.findByCohortIdOrderByDailyScoreDesc(cohortId);
                Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
                LocalDate today = LocalDate.now();
                LocalDate weekStartDate = today.minusDays(6);
        AtomicInteger rank = new AtomicInteger(1);
        return members.stream()
                                .map(m -> {
                                        long weeklyTaskCount = taskRepository.countByUserIdAndCompletedAtBetween(
                                                        m.getUser().getId(), weekAgo, Instant.now());
                                        long weeklyHabitCount = habitCompletionRepository.countByUserIdAndCompletedDateBetween(
                                                        m.getUser().getId(), weekStartDate, today);
                                        int weeklyScore = (int) (weeklyTaskCount * TASK_WEEKLY_SCORE + weeklyHabitCount * HABIT_WEEKLY_SCORE);

                                        return new LeaderboardEntry(
                                                        rank.getAndIncrement(),
                                                        m.getUser().getId(),
                                                        m.getUser().getUsername(),
                                                        m.getDailyScore(),
                                                        weeklyScore,
                                                        m.getFirstPlaceFinishes(),
                                                        m.getUser().getLevel());
                                })
                .toList();
    }

        @Transactional
        public void addScoreForUser(Long userId, int delta) {
                if (delta <= 0) {
                        return;
                }

                List<CohortMember> memberships = memberRepository.findByUserId(userId);
                if (memberships.isEmpty()) {
                        return;
                }

                for (CohortMember member : memberships) {
                        member.setDailyScore(member.getDailyScore() + delta);
                }
                memberRepository.saveAll(memberships);
        }

    @Transactional
    public void kickMember(String adminUsername, Long cohortId, Long targetUserId) {
        User admin = findUser(adminUsername);
        CohortMember adminMember = memberRepository.findByCohortIdAndUserId(cohortId, admin.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this cohort"));
        if (adminMember.getRole() != CohortRole.ADMIN) {
            throw new BadRequestException("Only cohort ADMINs can kick members");
        }
        CohortMember target = memberRepository.findByCohortIdAndUserId(cohortId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("CohortMember", "userId", targetUserId));
        memberRepository.delete(target);
    }

    // ── Scheduled: daily score reset ────────────────────────

    /**
     * CRON: midnight IST daily.
     * Resets all cohort members' daily scores to zero.
     * @Transactional is required because the repository method uses @Modifying.
     */
        @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void resetDailyScores() {
                int rewarded = rewardDailyTopMembers();
        int updated = memberRepository.resetAllDailyScores();
                log.info("Rewarded {} top cohort members and reset daily scores for {} members", rewarded, updated);
    }

        private int rewardDailyTopMembers() {
                int rewardedCount = 0;
                List<Cohort> cohorts = cohortRepository.findAll();

                for (Cohort cohort : cohorts) {
                        List<CohortMember> members = memberRepository.findByCohortIdOrderByDailyScoreDesc(cohort.getId());
                        if (members.isEmpty()) {
                                continue;
                        }

                        int topScore = members.get(0).getDailyScore();
                        if (topScore <= 0) {
                                continue;
                        }

                        for (CohortMember member : members) {
                                if (member.getDailyScore() != topScore) {
                                        break;
                                }

                                member.setFirstPlaceFinishes(member.getFirstPlaceFinishes() + 1);
                                User winner = member.getUser();
                                winner.setXp(winner.getXp() + DAILY_WIN_XP_REWARD);
                                winner.setCoins(winner.getCoins() + DAILY_WIN_COIN_REWARD);
                                userRepository.save(winner);
                                rewardedCount++;
                        }
                        memberRepository.saveAll(members);
                }

                return rewardedCount;
        }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private CohortResponse toCohortResponse(Cohort cohort) {
        List<CohortMember> members = memberRepository.findByCohortIdOrderByDailyScoreDesc(cohort.getId());
        List<CohortMemberInfo> memberInfos = members.stream()
                .map(m -> new CohortMemberInfo(
                        m.getUser().getId(), m.getUser().getUsername(),
                        m.getRole(), m.getDailyScore(),
                        m.getUser().getLevel(), m.getUser().getXp()))
                .toList();
        return new CohortResponse(
                cohort.getId(), cohort.getName(), cohort.getJoinCode(),
                memberInfos, cohort.getCreatedAt());
    }
}
