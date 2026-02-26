package com.mhrs.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mhrs.auth.application.command.ForgotPasswordCommand;
import com.mhrs.auth.application.command.LoginCommand;
import com.mhrs.auth.application.command.RegisterCommand;
import com.mhrs.auth.application.command.ResetPasswordCommand;
import com.mhrs.auth.application.port.out.AccessTokenProvider;
import com.mhrs.auth.application.port.out.CurrentUserProvider;
import com.mhrs.auth.application.port.out.PasswordHasher;
import com.mhrs.auth.application.port.out.PasswordResetTokenRepository;
import com.mhrs.auth.application.port.out.RefreshTokenRepository;
import com.mhrs.auth.application.port.out.RequestContextProvider;
import com.mhrs.auth.application.port.out.UserAccountRepository;
import com.mhrs.auth.application.port.out.UserSessionRepository;
import com.mhrs.auth.application.port.out.VerificationTokenRepository;
import com.mhrs.auth.domain.AuthTokens;
import com.mhrs.auth.domain.UserAccount;
import com.mhrs.auth.domain.UserRole;
import com.mhrs.auth.domain.UserSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AccessTokenProvider accessTokenProvider;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private RequestContextProvider requestContextProvider;

    @Mock
    private TokenGenerator tokenGenerator;

    private AuthApplicationService service;
    private AuthProperties authProperties;
    private Clock clock;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties(
            new AuthProperties.Jwt("test-secret-change-me-please-32bytes"),
            Duration.ofMinutes(15),
            Duration.ofDays(30),
            Duration.ofHours(24),
            Duration.ofHours(1)
        );
        clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"),
            ZoneOffset.UTC
        );
        service = new AuthApplicationService(
            userAccountRepository,
            userSessionRepository,
            refreshTokenRepository,
            verificationTokenRepository,
            passwordResetTokenRepository,
            passwordHasher,
            accessTokenProvider,
            currentUserProvider,
            requestContextProvider,
            tokenGenerator,
            authProperties,
            clock
        );
    }

    @Test
    void registerCreatesTokens() {
        Instant now = Instant.now(clock);
        when(userAccountRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("password123")).thenReturn("hash");
        UserAccount savedUser = new UserAccount(
            "u1",
            "a@b.com",
            "hash",
            "Ali",
            "Veli",
            UserRole.PATIENT,
            false,
            now,
            now
        );
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(requestContextProvider.clientIp()).thenReturn("127.0.0.1");
        when(requestContextProvider.userAgent()).thenReturn("agent");
        UserSession savedSession = new UserSession(
            "s1",
            "u1",
            "web",
            "127.0.0.1",
            "agent",
            now,
            now,
            null
        );
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(savedSession);
        when(tokenGenerator.generateToken()).thenReturn("refresh-token", "verify-token");
        when(tokenGenerator.hashToken("refresh-token")).thenReturn("refresh-hash");
        when(tokenGenerator.hashToken("verify-token")).thenReturn("verify-hash");
        when(accessTokenProvider.issueToken(
            eq(savedUser),
            eq("s1"),
            eq(now),
            eq(authProperties.accessTokenTtl())
        )).thenReturn("access-token");

        AuthTokens tokens = service.register(
            new RegisterCommand("a@b.com", "password123", "Ali", "Veli")
        );

        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());
        assertEquals("Bearer", tokens.tokenType());
        assertEquals(900, tokens.expiresInSeconds());
        verify(refreshTokenRepository).save(any());
        verify(verificationTokenRepository).save(any());
    }

    @Test
    void registerThrowsWhenEmailExists() {
        when(userAccountRepository.findByEmail("a@b.com"))
            .thenReturn(Optional.of(mockUser("u1")));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.register(
                new RegisterCommand("a@b.com", "password123", "Ali", "Veli")
            )
        );
    }

    @Test
    void loginThrowsWhenPasswordInvalid() {
        UserAccount user = mockUser("u1");
        when(userAccountRepository.findByEmail("a@b.com"))
            .thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", user.passwordHash()))
            .thenReturn(false);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.login(new LoginCommand("a@b.com", "password123"))
        );
        verifyNoInteractions(userSessionRepository);
    }

    @Test
    void forgotPasswordReturnsGenericMessageWhenUserMissing() {
        when(userAccountRepository.findByEmail("a@b.com"))
            .thenReturn(Optional.empty());

        String message = service.forgotPassword(new ForgotPasswordCommand("a@b.com"));

        assertEquals(
            "Password reset instructions sent if account exists.",
            message
        );
        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void resetPasswordThrowsWhenTokenInvalid() {
        when(tokenGenerator.hashToken("token-1")).thenReturn("hash-1");
        when(passwordResetTokenRepository.findByTokenHash("hash-1"))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.resetPassword(new ResetPasswordCommand("token-1", "password123"))
        );
    }

    private UserAccount mockUser(String userId) {
        Instant now = Instant.now(clock);
        return new UserAccount(
            userId,
            "a@b.com",
            "hash",
            "Ali",
            "Veli",
            UserRole.PATIENT,
            false,
            now,
            now
        );
    }
}
