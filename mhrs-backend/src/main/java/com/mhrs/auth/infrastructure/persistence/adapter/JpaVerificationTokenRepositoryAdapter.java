package com.mhrs.auth.infrastructure.persistence.adapter;

import com.mhrs.auth.application.port.out.VerificationTokenRepository;
import com.mhrs.auth.domain.VerificationToken;
import com.mhrs.auth.infrastructure.persistence.EmailVerificationTokenEntity;
import com.mhrs.auth.infrastructure.persistence.UserEntity;
import com.mhrs.auth.infrastructure.persistence.repository.EmailVerificationTokenJpaRepository;
import com.mhrs.auth.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaVerificationTokenRepositoryAdapter
    implements VerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository tokenRepository;
    private final UserJpaRepository userRepository;

    public JpaVerificationTokenRepositoryAdapter(
        EmailVerificationTokenJpaRepository tokenRepository,
        UserJpaRepository userRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        UserEntity user = userRepository
            .findById(parseUuid(token.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        EmailVerificationTokenEntity saved = tokenRepository.save(
            new EmailVerificationTokenEntity(
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
    public Optional<VerificationToken> findByTokenHash(String tokenHash) {
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

    private VerificationToken toDomain(EmailVerificationTokenEntity entity) {
        return new VerificationToken(
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
