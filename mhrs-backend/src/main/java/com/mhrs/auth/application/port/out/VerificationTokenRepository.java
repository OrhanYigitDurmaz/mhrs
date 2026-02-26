package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.VerificationToken;
import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    void markUsed(String tokenId, Instant usedAt);
}
