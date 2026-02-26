package com.mhrs.auth.application;

import com.mhrs.auth.application.command.ForgotPasswordCommand;
import com.mhrs.auth.application.command.LoginCommand;
import com.mhrs.auth.application.command.LogoutCommand;
import com.mhrs.auth.application.command.RefreshTokenCommand;
import com.mhrs.auth.application.command.RegisterCommand;
import com.mhrs.auth.application.command.ResetPasswordCommand;
import com.mhrs.auth.application.command.VerifyEmailCommand;
import com.mhrs.auth.application.port.out.AccessTokenProvider;
import com.mhrs.auth.application.port.out.CurrentUserProvider;
import com.mhrs.auth.application.port.out.PasswordHasher;
import com.mhrs.auth.application.port.out.PasswordResetTokenRepository;
import com.mhrs.auth.application.port.out.RefreshTokenRepository;
import com.mhrs.auth.application.port.out.RequestContextProvider;
import com.mhrs.auth.application.port.out.UserAccountRepository;
import com.mhrs.auth.application.port.out.UserSessionRepository;
import com.mhrs.auth.application.port.out.VerificationTokenRepository;
import com.mhrs.auth.domain.AuthSession;
import com.mhrs.auth.domain.AuthTokens;
import com.mhrs.auth.domain.AuthenticatedUser;
import com.mhrs.auth.domain.PasswordPolicy;
import com.mhrs.auth.domain.PasswordResetToken;
import com.mhrs.auth.domain.RefreshTokenRecord;
import com.mhrs.auth.domain.UserAccount;
import com.mhrs.auth.domain.UserRole;
import com.mhrs.auth.domain.UserSession;
import com.mhrs.auth.domain.VerificationToken;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService implements AuthUseCase {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String GENERIC_FORGOT_PASSWORD_MESSAGE =
        "Password reset instructions sent if account exists.";
    private static final String RESET_PASSWORD_MESSAGE =
        "Password has been reset.";
    private static final String VERIFY_EMAIL_MESSAGE = "Email verified.";
    private static final String DEFAULT_DEVICE = "web";

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenProvider accessTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final RequestContextProvider requestContextProvider;
    private final TokenGenerator tokenGenerator;
    private final AuthProperties authProperties;
    private final Clock clock;

    public AuthApplicationService(
        UserAccountRepository userAccountRepository,
        UserSessionRepository userSessionRepository,
        RefreshTokenRepository refreshTokenRepository,
        VerificationTokenRepository verificationTokenRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        PasswordHasher passwordHasher,
        AccessTokenProvider accessTokenProvider,
        CurrentUserProvider currentUserProvider,
        RequestContextProvider requestContextProvider,
        TokenGenerator tokenGenerator,
        AuthProperties authProperties,
        Clock clock
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenProvider = accessTokenProvider;
        this.currentUserProvider = currentUserProvider;
        this.requestContextProvider = requestContextProvider;
        this.tokenGenerator = tokenGenerator;
        this.authProperties = authProperties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AuthTokens register(RegisterCommand command) {
        PasswordPolicy.validate(command.password());
        String email = normalizeEmail(command.email());
        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }

        Instant now = clock.instant();
        UserAccount user = new UserAccount(
            null,
            email,
            passwordHasher.hash(command.password()),
            command.firstName(),
            command.lastName(),
            UserRole.PATIENT,
            false,
            now,
            now
        );
        UserAccount savedUser = userAccountRepository.save(user);

        UserSession session = createSession(savedUser.id(), now);
        UserSession savedSession = userSessionRepository.save(session);

        String refreshToken = issueRefreshToken(savedSession.id(), now);
        issueVerificationToken(savedUser.id(), now);

        String accessToken = accessTokenProvider.issueToken(
            savedUser,
            savedSession.id(),
            now,
            authProperties.accessTokenTtl()
        );

        return new AuthTokens(
            accessToken,
            refreshToken,
            TOKEN_TYPE,
            authProperties.accessTokenTtl().toSeconds()
        );
    }

    @Override
    @Transactional
    public AuthTokens login(LoginCommand command) {
        PasswordPolicy.validate(command.password());
        String email = normalizeEmail(command.email());
        UserAccount user = userAccountRepository
            .findByEmail(email)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid credentials.")
            );

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        Instant now = clock.instant();
        UserSession session = createSession(user.id(), now);
        UserSession savedSession = userSessionRepository.save(session);

        String refreshToken = issueRefreshToken(savedSession.id(), now);
        String accessToken = accessTokenProvider.issueToken(
            user,
            savedSession.id(),
            now,
            authProperties.accessTokenTtl()
        );

        return new AuthTokens(
            accessToken,
            refreshToken,
            TOKEN_TYPE,
            authProperties.accessTokenTtl().toSeconds()
        );
    }

    @Override
    @Transactional
    public AuthTokens refresh(RefreshTokenCommand command) {
        Instant now = clock.instant();
        String tokenHash = tokenGenerator.hashToken(command.refreshToken());
        RefreshTokenRecord existing = refreshTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid refresh token.")
            );

        if (
            isTokenInvalid(
                existing.expiresAt(),
                existing.revokedAt(),
                existing.rotatedAt(),
                now
            )
        ) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        UserSession session = userSessionRepository
            .findById(existing.sessionId())
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid refresh token.")
            );

        if (session.revokedAt() != null) {
            throw new IllegalArgumentException("Session revoked.");
        }

        refreshTokenRepository.revoke(existing.id(), now, now);
        String newRefreshToken = issueRefreshToken(session.id(), now);
        userSessionRepository.updateLastSeen(session.id(), now);

        UserAccount user = userAccountRepository
            .findById(session.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String accessToken = accessTokenProvider.issueToken(
            user,
            session.id(),
            now,
            authProperties.accessTokenTtl()
        );

        return new AuthTokens(
            accessToken,
            newRefreshToken,
            TOKEN_TYPE,
            authProperties.accessTokenTtl().toSeconds()
        );
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        Instant now = clock.instant();
        String tokenHash = tokenGenerator.hashToken(command.refreshToken());
        Optional<RefreshTokenRecord> token =
            refreshTokenRepository.findByTokenHash(tokenHash);
        if (token.isEmpty()) {
            return;
        }

        RefreshTokenRecord record = token.get();
        refreshTokenRepository.revoke(record.id(), now, null);
        refreshTokenRepository.revokeBySession(record.sessionId(), now);
        userSessionRepository.revokeSession(record.sessionId(), now);
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordCommand command) {
        String email = normalizeEmail(command.email());
        Optional<UserAccount> user = userAccountRepository.findByEmail(email);
        if (user.isPresent()) {
            Instant now = clock.instant();
            String tokenValue = tokenGenerator.generateToken();
            String tokenHash = tokenGenerator.hashToken(tokenValue);
            passwordResetTokenRepository.save(
                new PasswordResetToken(
                    null,
                    user.get().id(),
                    tokenHash,
                    now.plus(authProperties.resetTokenTtl()),
                    null,
                    now
                )
            );
        }
        return GENERIC_FORGOT_PASSWORD_MESSAGE;
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordCommand command) {
        PasswordPolicy.validate(command.newPassword());
        Instant now = clock.instant();
        String tokenHash = tokenGenerator.hashToken(command.token());
        PasswordResetToken token = passwordResetTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid reset token.")
            );

        if (isTokenInvalid(token.expiresAt(), token.usedAt(), null, now)) {
            throw new IllegalArgumentException("Invalid reset token.");
        }

        UserAccount user = userAccountRepository
            .findById(token.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        UserAccount updated = new UserAccount(
            user.id(),
            user.email(),
            passwordHasher.hash(command.newPassword()),
            user.firstName(),
            user.lastName(),
            user.role(),
            user.emailVerified(),
            user.createdAt(),
            now
        );
        userAccountRepository.update(updated);
        passwordResetTokenRepository.markUsed(token.id(), now);
        userSessionRepository.revokeAllForUser(user.id(), now);
        refreshTokenRepository.revokeAllForUser(user.id(), now);

        return RESET_PASSWORD_MESSAGE;
    }

    @Override
    @Transactional
    public String verifyEmail(VerifyEmailCommand command) {
        Instant now = clock.instant();
        String tokenHash = tokenGenerator.hashToken(command.token());
        VerificationToken token = verificationTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid verification token.")
            );

        if (isTokenInvalid(token.expiresAt(), token.usedAt(), null, now)) {
            throw new IllegalArgumentException("Invalid verification token.");
        }

        UserAccount user = userAccountRepository
            .findById(token.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        UserAccount updated = new UserAccount(
            user.id(),
            user.email(),
            user.passwordHash(),
            user.firstName(),
            user.lastName(),
            user.role(),
            true,
            user.createdAt(),
            now
        );
        userAccountRepository.update(updated);
        verificationTokenRepository.markUsed(token.id(), now);

        return VERIFY_EMAIL_MESSAGE;
    }

    @Override
    public AuthenticatedUser me() {
        return currentUserProvider.currentUser();
    }

    @Override
    public List<AuthSession> sessions() {
        AuthenticatedUser currentUser = currentUserProvider.currentUser();
        String currentSessionId = currentUserProvider.currentSessionId();
        return userSessionRepository
            .findByUserId(currentUser.userId())
            .stream()
            .map(session ->
                new AuthSession(
                    session.id(),
                    session.device(),
                    session.ipAddress(),
                    session.createdAt(),
                    session.lastSeenAt(),
                    session.id().equals(currentSessionId)
                )
            )
            .toList();
    }

    @Override
    @Transactional
    public void revokeSession(String sessionId) {
        AuthenticatedUser currentUser = currentUserProvider.currentUser();
        UserSession session = userSessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException("Session not found.")
            );
        if (!session.userId().equals(currentUser.userId())) {
            throw new IllegalArgumentException("Session not found.");
        }
        Instant now = clock.instant();
        userSessionRepository.revokeSession(sessionId, now);
        refreshTokenRepository.revokeBySession(sessionId, now);
    }

    @Override
    @Transactional
    public void logoutAll() {
        AuthenticatedUser currentUser = currentUserProvider.currentUser();
        Instant now = clock.instant();
        userSessionRepository.revokeAllForUser(currentUser.userId(), now);
        refreshTokenRepository.revokeAllForUser(currentUser.userId(), now);
    }

    private UserSession createSession(String userId, Instant now) {
        return new UserSession(
            null,
            userId,
            DEFAULT_DEVICE,
            requestContextProvider.clientIp(),
            requestContextProvider.userAgent(),
            now,
            now,
            null
        );
    }

    private String issueRefreshToken(String sessionId, Instant now) {
        String tokenValue = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(tokenValue);
        refreshTokenRepository.save(
            new RefreshTokenRecord(
                null,
                sessionId,
                tokenHash,
                now.plus(authProperties.refreshTokenTtl()),
                null,
                null,
                now
            )
        );
        return tokenValue;
    }

    private void issueVerificationToken(String userId, Instant now) {
        String tokenValue = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(tokenValue);
        verificationTokenRepository.save(
            new VerificationToken(
                null,
                userId,
                tokenHash,
                now.plus(authProperties.verificationTokenTtl()),
                null,
                now
            )
        );
    }

    private boolean isTokenInvalid(
        Instant expiresAt,
        Instant revokedAt,
        Instant rotatedAt,
        Instant now
    ) {
        if (expiresAt != null && expiresAt.isBefore(now)) {
            return true;
        }
        if (revokedAt != null) {
            return true;
        }
        return rotatedAt != null;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
