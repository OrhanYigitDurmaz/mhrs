package com.mhrs.auth.api;

import com.mhrs.auth.api.dto.AuthMeResponse;
import com.mhrs.auth.api.dto.AuthSessionResponse;
import com.mhrs.auth.api.dto.AuthTokensResponse;
import com.mhrs.auth.api.dto.ForgotPasswordRequest;
import com.mhrs.auth.api.dto.LoginRequest;
import com.mhrs.auth.api.dto.LogoutRequest;
import com.mhrs.auth.api.dto.MessageResponse;
import com.mhrs.auth.api.dto.RefreshTokenRequest;
import com.mhrs.auth.api.dto.RegisterRequest;
import com.mhrs.auth.api.dto.ResetPasswordRequest;
import com.mhrs.auth.api.dto.VerifyEmailRequest;
import com.mhrs.auth.application.AuthUseCase;
import com.mhrs.auth.application.command.ForgotPasswordCommand;
import com.mhrs.auth.application.command.LoginCommand;
import com.mhrs.auth.application.command.LogoutCommand;
import com.mhrs.auth.application.command.RefreshTokenCommand;
import com.mhrs.auth.application.command.RegisterCommand;
import com.mhrs.auth.application.command.ResetPasswordCommand;
import com.mhrs.auth.application.command.VerifyEmailCommand;
import com.mhrs.auth.domain.AuthSession;
import com.mhrs.auth.domain.AuthTokens;
import com.mhrs.auth.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        AuthTokens tokens = authUseCase.register(
            new RegisterCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
            )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
            toTokensResponse(tokens)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthTokens tokens = authUseCase.login(
            new LoginCommand(request.email(), request.password())
        );
        return ResponseEntity.ok(toTokensResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokens tokens = authUseCase.refresh(
            new RefreshTokenCommand(request.refreshToken())
        );
        return ResponseEntity.ok(toTokensResponse(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @Valid @RequestBody LogoutRequest request
    ) {
        authUseCase.logout(new LogoutCommand(request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        String message = authUseCase.forgotPassword(
            new ForgotPasswordCommand(request.email())
        );
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        String message = authUseCase.resetPassword(
            new ResetPasswordCommand(request.token(), request.newPassword())
        );
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
        @Valid @RequestBody VerifyEmailRequest request
    ) {
        String message = authUseCase.verifyEmail(
            new VerifyEmailCommand(request.token())
        );
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me() {
        AuthenticatedUser user = authUseCase.me();
        return ResponseEntity.ok(
            new AuthMeResponse(
                user.userId(),
                user.email(),
                user.role().name(),
                user.emailVerified()
            )
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AuthSessionResponse>> sessions() {
        List<AuthSessionResponse> sessions = authUseCase
            .sessions()
            .stream()
            .map(this::toSessionResponse)
            .toList();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}/revoke")
    public ResponseEntity<Void> revokeSession(@PathVariable String sessionId) {
        authUseCase.revokeSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll() {
        authUseCase.logoutAll();
        return ResponseEntity.noContent().build();
    }

    private AuthTokensResponse toTokensResponse(AuthTokens tokens) {
        return new AuthTokensResponse(
            tokens.accessToken(),
            tokens.refreshToken(),
            tokens.tokenType(),
            tokens.expiresInSeconds()
        );
    }

    private AuthSessionResponse toSessionResponse(AuthSession session) {
        return new AuthSessionResponse(
            session.sessionId(),
            session.device(),
            session.ipAddress(),
            session.createdAt(),
            session.lastSeenAt(),
            session.current()
        );
    }
}
