package com.mhrs.auth.infrastructure.persistence.adapter;

import com.mhrs.auth.application.port.out.PasswordResetTokenRepository;
import com.mhrs.auth.domain.PasswordResetToken;
import com.mhrs.auth.infrastructure.persistence.PasswordResetTokenEntity;
import com.mhrs.auth.infrastructure.persistence.UserEntity;
import com.mhrs.auth.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import com.mhrs.auth.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaPasswordResetTokenRepositoryAdapter
    implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository tokenRepository;
    private final UserJpaRepository userRepository;

    public JpaPasswordResetTokenRepositoryAdapter(
        PasswordResetTokenJpaRepository tokenRepository,
        UserJpaRepository userRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        UserEntity user = userRepository
            .findById(parseUuid(token.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        PasswordResetTokenEntity saved = tokenRepository.save(
            new PasswordResetTokenEntity(
                token.id() == null ? null : parseUuid(token.id()),
                user,
                token.tokenHash(),
                token.expiresAt(),
                token.usedAt(),
                token.createdAt()
            )
        );
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return tokenRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void markUsed(String tokenId, Instant usedAt) {
        tokenRepository
            .findById(parseUuid(tokenId))
            .ifPresent(token -> {
                token.setUsedAt(usedAt);
                tokenRepository.save(token);
            });
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
            entity.getId().toString(),
            entity.getUser().getId().toString(),
            entity.getTokenHash(),
            entity.getExpiresAt(),
            entity.getUsedAt(),
            entity.getCreatedAt()
        );
    }

    private UUID parseUuid(String value) {
        return UUID.fromString(value);
    }
}
