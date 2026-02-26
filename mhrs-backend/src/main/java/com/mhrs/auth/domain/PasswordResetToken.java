package com.mhrs.auth.domain;

import java.time.Instant;

public record PasswordResetToken(
        String id,
        String userId,
        String tokenHash,
        Instant expiresAt,
        Instant usedAt,
        Instant createdAt
) {
}
