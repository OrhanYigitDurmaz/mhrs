package com.mhrs.auth.domain;

import java.time.Instant;

public record UserAccount(
        String id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        UserRole role,
        boolean emailVerified,
        Instant createdAt,
        Instant updatedAt
) {
}
