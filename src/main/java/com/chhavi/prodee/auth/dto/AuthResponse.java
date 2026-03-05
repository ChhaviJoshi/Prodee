package com.chhavi.prodee.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username
) {
    public AuthResponse(String token, Long userId, String username) {
        this(token, "Bearer", userId, username);
    }
}
