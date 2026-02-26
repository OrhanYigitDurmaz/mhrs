package com.mhrs.auth.infrastructure.persistence.repository;

import com.mhrs.auth.infrastructure.persistence.RefreshTokenEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository
    extends JpaRepository<RefreshTokenEntity, UUID>
{
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findBySession_Id(UUID sessionId);

    List<RefreshTokenEntity> findBySession_User_Id(UUID userId);
}
