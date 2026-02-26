package com.mhrs.auth.infrastructure.persistence.repository;

import com.mhrs.auth.infrastructure.persistence.AuthSessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionJpaRepository
    extends JpaRepository<AuthSessionEntity, UUID>
{
    List<AuthSessionEntity> findByUser_Id(UUID userId);
}
