package com.mhrs.auth.infrastructure.persistence.adapter;

import com.mhrs.auth.application.port.out.RefreshTokenRepository;
import com.mhrs.auth.domain.RefreshTokenRecord;
import com.mhrs.auth.infrastructure.persistence.AuthSessionEntity;
import com.mhrs.auth.infrastructure.persistence.RefreshTokenEntity;
import com.mhrs.auth.infrastructure.persistence.repository.AuthSessionJpaRepository;
import com.mhrs.auth.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaRefreshTokenRepositoryAdapter
    implements RefreshTokenRepository
{

    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final AuthSessionJpaRepository sessionRepository;

    public JpaRefreshTokenRepositoryAdapter(
        RefreshTokenJpaRepository refreshTokenRepository,
        AuthSessionJpaRepository sessionRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public RefreshTokenRecord save(RefreshTokenRecord token) {
        AuthSessionEntity session = sessionRepository
            .findById(parseUuid(token.sessionId()))
            .orElseThrow(() ->
                new IllegalArgumentException("Session not found.")
            );
        RefreshTokenEntity saved = refreshTokenRepository.save(
            new RefreshTokenEntity(
                token.id() == null ? null : parseUuid(token.id()),
                session,
                token.tokenHash(),
                token.expiresAt(),
                token.revokedAt(),
                token.rotatedAt(),
                token.createdAt()
            )
        );
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        return refreshTokenRepository
            .findByTokenHash(tokenHash)
            .map(this::toDomain);
    }

    @Override
    public void revoke(String tokenId, Instant revokedAt, Instant rotatedAt) {
        refreshTokenRepository
            .findById(parseUuid(tokenId))
            .ifPresent(token -> {
                token.setRevokedAt(revokedAt);
                token.setRotatedAt(rotatedAt);
                refreshTokenRepository.save(token);
            });
    }

    @Override
    public void revokeBySession(String sessionId, Instant revokedAt) {
        List<RefreshTokenEntity> tokens =
            refreshTokenRepository.findBySession_Id(parseUuid(sessionId));
        for (RefreshTokenEntity token : tokens) {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(revokedAt);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public void revokeAllForUser(String userId, Instant revokedAt) {
        List<RefreshTokenEntity> tokens =
            refreshTokenRepository.findBySession_User_Id(parseUuid(userId));
        for (RefreshTokenEntity token : tokens) {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(revokedAt);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }

    private RefreshTokenRecord toDomain(RefreshTokenEntity entity) {
        return new RefreshTokenRecord(
            entity.getId().toString(),
            entity.getSession().getId().toString(),
            entity.getTokenHash(),
            entity.getExpiresAt(),
            entity.getRevokedAt(),
            entity.getRotatedAt(),
            entity.getCreatedAt()
        );
    }

    private UUID parseUuid(String value) {
        return UUID.fromString(value);
    }
}
