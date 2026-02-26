package com.mhrs.auth.infrastructure.persistence.adapter;

import com.mhrs.auth.application.port.out.UserAccountRepository;
import com.mhrs.auth.domain.UserAccount;
import com.mhrs.auth.infrastructure.persistence.UserEntity;
import com.mhrs.auth.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaUserAccountRepositoryAdapter implements UserAccountRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaUserAccountRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(String userId) {
        return userJpaRepository.findById(parseUuid(userId)).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        UserEntity saved = userJpaRepository.save(toEntity(userAccount));
        return toDomain(saved);
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        UserEntity saved = userJpaRepository.save(toEntity(userAccount));
        return toDomain(saved);
    }

    private UserEntity toEntity(UserAccount user) {
        return new UserEntity(
            user.id() == null ? null : parseUuid(user.id()),
            user.email(),
            user.passwordHash(),
            user.firstName(),
            user.lastName(),
            user.role(),
            user.emailVerified(),
            user.createdAt(),
            user.updatedAt()
        );
    }

    private UserAccount toDomain(UserEntity entity) {
        return new UserAccount(
            entity.getId().toString(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getRole(),
            entity.isEmailVerified(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private UUID parseUuid(String value) {
        return UUID.fromString(value);
    }
}
