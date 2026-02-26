package com.mhrs.auth.infrastructure.security;

import com.mhrs.auth.domain.UserRole;

public record AuthPrincipal(
        String userId,
        String email,
        UserRole role,
        boolean emailVerified,
        String sessionId
) {
}
