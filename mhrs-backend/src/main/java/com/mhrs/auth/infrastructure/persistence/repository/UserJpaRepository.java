package com.mhrs.auth.infrastructure.persistence.repository;

import com.mhrs.auth.infrastructure.persistence.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);
}
