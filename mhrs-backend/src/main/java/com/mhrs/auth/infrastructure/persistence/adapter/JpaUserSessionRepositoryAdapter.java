package com.mhrs.auth.infrastructure.persistence.adapter;

import com.mhrs.auth.application.port.out.UserSessionRepository;
import com.mhrs.auth.domain.UserSession;
import com.mhrs.auth.infrastructure.persistence.AuthSessionEntity;
import com.mhrs.auth.infrastructure.persistence.UserEntity;
import com.mhrs.auth.infrastructure.persistence.repository.AuthSessionJpaRepository;
import com.mhrs.auth.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaUserSessionRepositoryAdapter implements UserSessionRepository {

    private final AuthSessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;

    public JpaUserSessionRepositoryAdapter(
        AuthSessionJpaRepository sessionRepository,
        UserJpaRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserSession save(UserSession session) {
        UserEntity user = userRepository
            .findById(parseUuid(session.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        AuthSessionEntity saved = sessionRepository.save(
            new AuthSessionEntity(
                session.id() == null ? null : parseUuid(session.id()),
                user,
                session.device(),
                session.ipAddress(),
                session.userAgent(),
                session.createdAt(),
                session.lastSeenAt(),
                session.revokedAt()
            )
        );
        return toDomain(saved);
    }

    @Override
    public Optional<UserSession> findById(String sessionId) {
        return sessionRepository
            .findById(parseUuid(sessionId))
            .map(this::toDomain);
    }

    @Override
    public List<UserSession> findByUserId(String userId) {
        return sessionRepository
            .findByUser_Id(parseUuid(userId))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void updateLastSeen(String sessionId, Instant lastSeenAt) {
        sessionRepository
            .findById(parseUuid(sessionId))
            .ifPresent(session -> {
                session.setLastSeenAt(lastSeenAt);
                sessionRepository.save(session);
            });
    }

    @Override
    public void revokeSession(String sessionId, Instant revokedAt) {
        sessionRepository
            .findById(parseUuid(sessionId))
            .ifPresent(session -> {
                session.setRevokedAt(revokedAt);
                sessionRepository.save(session);
            });
    }

    @Override
    public void revokeAllForUser(String userId, Instant revokedAt) {
        List<AuthSessionEntity> sessions = sessionRepository.findByUser_Id(
            parseUuid(userId)
        );
        for (AuthSessionEntity session : sessions) {
            session.setRevokedAt(revokedAt);
        }
        sessionRepository.saveAll(sessions);
    }

    private UserSession toDomain(AuthSessionEntity entity) {
        return new UserSession(
            entity.getId().toString(),
            entity.getUser().getId().toString(),
            entity.getDevice(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt(),
            entity.getLastSeenAt(),
            entity.getRevokedAt()
        );
    }

    private UUID parseUuid(String value) {
        return UUID.fromString(value);
    }
}
