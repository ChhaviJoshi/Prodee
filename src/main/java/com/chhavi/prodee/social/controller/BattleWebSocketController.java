package com.chhavi.prodee.social.controller;

import com.chhavi.prodee.social.dto.BattleProgressMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for the "Ghost Mode" Cohort Battle.
 *
 * Client sends:     /app/battle.progress
 * Server broadcasts: /topic/battle/{cohortId}
 *
 * Every ~500ms the game client sends the player's current distance.
 * The server simply relays the message to all cohort members so they
 * can update the live progress bar.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BattleWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/battle.progress")
    public void handleBattleProgress(@Payload BattleProgressMessage message) {
        log.debug("Battle progress: {} at {} meters (cohort {})",
                message.username(), message.distanceMeters(), message.cohortId());

        // Broadcast to all members of the same cohort
        messagingTemplate.convertAndSend(
                "/topic/battle/" + message.cohortId(),
                message);
    }
}
