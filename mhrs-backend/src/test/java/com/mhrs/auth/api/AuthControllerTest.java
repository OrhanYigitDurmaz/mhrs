package com.mhrs.auth.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.mhrs.auth.domain.UserRole;
import com.mhrs.config.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(
    {
        SecurityConfig.class,
        com.mhrs.auth.infrastructure.security.JwtAuthenticationFilter.class,
    }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void registerReturnsCreatedAndTokens() throws Exception {
        when(
            authUseCase.register(
                new RegisterCommand("a@b.com", "password123", "Ali", "Veli")
            )
        ).thenReturn(new AuthTokens("access-1", "refresh-1", "Bearer", 900));

        String body =
            "{\"email\":\"a@b.com\",\"password\":\"password123\",\"firstName\":\"Ali\",\"lastName\":\"Veli\"}";

        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-1"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-1"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresInSeconds").value(900));

        verify(authUseCase).register(
            new RegisterCommand("a@b.com", "password123", "Ali", "Veli")
        );
    }

    @Test
    void registerWithInvalidEmailReturnsBadRequest() throws Exception {
        String body =
            "{\"email\":\"invalid-email\",\"password\":\"password123\",\"firstName\":\"Ali\",\"lastName\":\"Veli\"}";

        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsOkAndTokens() throws Exception {
        when(
            authUseCase.login(new LoginCommand("a@b.com", "password123"))
        ).thenReturn(new AuthTokens("access-2", "refresh-2", "Bearer", 900));

        String body = "{\"email\":\"a@b.com\",\"password\":\"password123\"}";

        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-2"));

        verify(authUseCase).login(new LoginCommand("a@b.com", "password123"));
    }

    @Test
    void refreshReturnsOkAndTokens() throws Exception {
        when(
            authUseCase.refresh(new RefreshTokenCommand("refresh-token"))
        ).thenReturn(new AuthTokens("access-3", "refresh-3", "Bearer", 900));

        String body = "{\"refreshToken\":\"refresh-token\"}";

        mockMvc
            .perform(
                post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-3"));

        verify(authUseCase).refresh(new RefreshTokenCommand("refresh-token"));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        String body = "{\"refreshToken\":\"refresh-token\"}";

        mockMvc
            .perform(
                post("/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isNoContent());

        verify(authUseCase).logout(new LogoutCommand("refresh-token"));
    }

    @Test
    void forgotPasswordReturnsMessage() throws Exception {
        when(
            authUseCase.forgotPassword(new ForgotPasswordCommand("a@b.com"))
        ).thenReturn("reset sent");

        String body = "{\"email\":\"a@b.com\"}";

        mockMvc
            .perform(
                post("/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("reset sent"));

        verify(authUseCase).forgotPassword(
            new ForgotPasswordCommand("a@b.com")
        );
    }

    @Test
    void resetPasswordReturnsMessage() throws Exception {
        when(
            authUseCase.resetPassword(
                new ResetPasswordCommand("token-1", "newpass123")
            )
        ).thenReturn("reset ok");

        String body = "{\"token\":\"token-1\",\"newPassword\":\"newpass123\"}";

        mockMvc
            .perform(
                post("/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("reset ok"));

        verify(authUseCase).resetPassword(
            new ResetPasswordCommand("token-1", "newpass123")
        );
    }

    @Test
    void verifyEmailReturnsMessage() throws Exception {
        when(
            authUseCase.verifyEmail(new VerifyEmailCommand("token-1"))
        ).thenReturn("verified");

        String body = "{\"token\":\"token-1\"}";

        mockMvc
            .perform(
                post("/auth/verify-email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("verified"));

        verify(authUseCase).verifyEmail(new VerifyEmailCommand("token-1"));
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        when(authUseCase.me()).thenReturn(
            new AuthenticatedUser("u1", "a@b.com", UserRole.ADMIN, true)
        );
        mockJwt("access-1", "u1", "s1", UserRole.ADMIN, true);

        mockMvc
            .perform(get("/auth/me").header("Authorization", "Bearer access-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void sessionsReturnsSessionList() throws Exception {
        when(authUseCase.sessions()).thenReturn(
            List.of(
                new AuthSession(
                    "s1",
                    "web",
                    "127.0.0.1",
                    Instant.parse("2026-01-01T00:00:00Z"),
                    Instant.parse("2026-01-01T01:00:00Z"),
                    true
                )
            )
        );
        mockJwt("access-2", "u1", "s1", UserRole.PATIENT, true);

        mockMvc
            .perform(
                get("/auth/sessions").header("Authorization", "Bearer access-2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].sessionId").value("s1"))
            .andExpect(jsonPath("$[0].current").value(true));
    }

    @Test
    void revokeSessionReturnsNoContent() throws Exception {
        mockJwt("access-3", "u1", "s1", UserRole.PATIENT, true);
        mockMvc
            .perform(
                delete("/auth/sessions/{sessionId}/revoke", "s1").header(
                    "Authorization",
                    "Bearer access-3"
                )
            )
            .andExpect(status().isNoContent());

        verify(authUseCase).revokeSession("s1");
    }

    @Test
    void logoutAllReturnsNoContent() throws Exception {
        mockJwt("access-4", "u1", "s1", UserRole.PATIENT, true);
        mockMvc
            .perform(
                post("/auth/logout-all").header(
                    "Authorization",
                    "Bearer access-4"
                )
            )
            .andExpect(status().isNoContent());

        verify(authUseCase).logoutAll();
    }

    @Test
    void illegalArgumentFromUseCaseMapsToBadRequest() throws Exception {
        Mockito.when(
            authUseCase.register(
                new RegisterCommand("a@b.com", "password123", "Ali", "Veli")
            )
        ).thenThrow(new IllegalArgumentException("bad input"));

        String body =
            "{\"email\":\"a@b.com\",\"password\":\"password123\",\"firstName\":\"Ali\",\"lastName\":\"Veli\"}";

        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("bad input"));
    }

    private void mockJwt(
        String token,
        String userId,
        String sessionId,
        UserRole role,
        boolean emailVerified
    ) {
        Jwt jwt = Jwt.withTokenValue(token)
            .header("alg", "HS256")
            .subject(userId)
            .claim("email", "a@b.com")
            .claim("role", role.name())
            .claim("email_verified", emailVerified)
            .claim("sid", sessionId)
            .build();
        when(jwtDecoder.decode(token)).thenReturn(jwt);
    }
}
