package com.mhrs.auth.infrastructure.security;

import com.mhrs.auth.application.port.out.AccessTokenProvider;
import com.mhrs.auth.domain.UserAccount;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenProvider implements AccessTokenProvider {

    private final JwtEncoder jwtEncoder;

    public JwtAccessTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public String issueToken(
        UserAccount user,
        String sessionId,
        Instant now,
        Duration ttl
    ) {
        Instant expiresAt = now.plus(ttl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.id())
            .claim("email", user.email())
            .claim("role", user.role().name())
            .claim("email_verified", user.emailVerified())
            .claim("sid", sessionId)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder
            .encode(JwtEncoderParameters.from(headers, claims))
            .getTokenValue();
    }
}
