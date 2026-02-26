package com.mhrs.auth.domain;

import java.time.Instant;

public record AuthSession(
        String sessionId,
        String device,
        String ipAddress,
        Instant createdAt,
        Instant lastSeenAt,
        boolean current
) {
}
