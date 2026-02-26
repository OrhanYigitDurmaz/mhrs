package com.mhrs.auth.application;

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
import java.util.List;

public interface AuthUseCase {

    AuthTokens register(RegisterCommand command);

    AuthTokens login(LoginCommand command);

    AuthTokens refresh(RefreshTokenCommand command);

    void logout(LogoutCommand command);

    String forgotPassword(ForgotPasswordCommand command);

    String resetPassword(ResetPasswordCommand command);

    String verifyEmail(VerifyEmailCommand command);

    AuthenticatedUser me();

    List<AuthSession> sessions();

    void revokeSession(String sessionId);

    void logoutAll();
}
