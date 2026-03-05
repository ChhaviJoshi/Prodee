package com.chhavi.prodee.social.dto;

/**
 * WebSocket message sent by game clients during a Ghost Mode battle.
 * Contains the player's current distance in the endless runner.
 */
public record BattleProgressMessage(
        Long cohortId,
        Long userId,
        String username,
        double distanceMeters
) {}
