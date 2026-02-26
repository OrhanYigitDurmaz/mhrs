package com.mhrs.auth.domain;

import java.time.Instant;

public record RefreshTokenRecord(
        String id,
        String sessionId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt,
        Instant rotatedAt,
        Instant createdAt
) {
}
