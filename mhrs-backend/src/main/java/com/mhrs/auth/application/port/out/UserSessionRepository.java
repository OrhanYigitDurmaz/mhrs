package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository {

    UserSession save(UserSession session);

    Optional<UserSession> findById(String sessionId);

    List<UserSession> findByUserId(String userId);

    void updateLastSeen(String sessionId, Instant lastSeenAt);

    void revokeSession(String sessionId, Instant revokedAt);

    void revokeAllForUser(String userId, Instant revokedAt);
}
