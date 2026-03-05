package com.chhavi.prodee.productivity.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.productivity.dto.FocusSessionRequest;
import com.chhavi.prodee.productivity.dto.FocusSessionResponse;
import com.chhavi.prodee.productivity.entity.FocusSession;
import com.chhavi.prodee.productivity.repository.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FocusSessionService {

    private final FocusSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public FocusSessionResponse logSession(String username, FocusSessionRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        double efficiency = Math.min(100.0,
                ((double) request.actualDurationMinutes() / request.expectedDurationMinutes()) * 100);

        FocusSession session = FocusSession.builder()
                .user(user)
                .expectedDurationMinutes(request.expectedDurationMinutes())
                .actualDurationMinutes(request.actualDurationMinutes())
                .efficiencyScore(Math.round(efficiency * 100.0) / 100.0)
                .ambientType(request.ambientType())
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .build();

        return toResponse(sessionRepository.save(session));
    }

    public List<FocusSessionResponse> getUserSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<FocusSessionResponse> getWeeklySessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        return sessionRepository.findByUserIdAndStartedAtBetween(user.getId(), weekAgo, Instant.now())
                .stream().map(this::toResponse).toList();
    }

    private FocusSessionResponse toResponse(FocusSession s) {
        return new FocusSessionResponse(
                s.getId(), s.getExpectedDurationMinutes(), s.getActualDurationMinutes(),
                s.getEfficiencyScore(), s.getAmbientType(), s.getStartedAt(), s.getEndedAt());
    }
}
