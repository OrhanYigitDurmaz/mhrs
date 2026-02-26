package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.RefreshTokenRecord;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshTokenRecord save(RefreshTokenRecord token);

    Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);

    void revoke(String tokenId, Instant revokedAt, Instant rotatedAt);

    void revokeBySession(String sessionId, Instant revokedAt);

    void revokeAllForUser(String userId, Instant revokedAt);
}
