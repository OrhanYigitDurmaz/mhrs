package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void markUsed(String tokenId, Instant usedAt);
}
