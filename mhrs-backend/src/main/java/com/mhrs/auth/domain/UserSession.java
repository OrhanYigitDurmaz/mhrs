package com.mhrs.auth.domain;

import java.time.Instant;

public record UserSession(
        String id,
        String userId,
        String device,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Instant lastSeenAt,
        Instant revokedAt
) {
}
