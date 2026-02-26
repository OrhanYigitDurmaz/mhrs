package com.mhrs.auth.application.port.out;

import com.mhrs.auth.domain.UserAccount;
import java.time.Duration;
import java.time.Instant;

public interface AccessTokenProvider {

    String issueToken(
        UserAccount user,
        String sessionId,
        Instant now,
        Duration ttl
    );
}
