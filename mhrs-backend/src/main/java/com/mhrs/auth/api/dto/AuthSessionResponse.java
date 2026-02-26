package com.mhrs.auth.api.dto;

import java.time.Instant;

public record AuthSessionResponse(
        String sessionId,
        String device,
        String ipAddress,
        Instant createdAt,
        Instant lastSeenAt,
        boolean current
) {
}
