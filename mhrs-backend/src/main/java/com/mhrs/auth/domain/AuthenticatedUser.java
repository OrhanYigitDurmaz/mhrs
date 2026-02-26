package com.mhrs.auth.domain;

public record AuthenticatedUser(
        String userId,
        String email,
        UserRole role,
        boolean emailVerified
) {
}
