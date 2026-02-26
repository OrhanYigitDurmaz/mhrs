package com.mhrs.auth.api.dto;

public record AuthMeResponse(
        String userId,
        String email,
        String role,
        boolean emailVerified
) {
}
