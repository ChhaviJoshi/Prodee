package com.chhavi.prodee.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "Token is required")
    String token
) {}
