package com.mhrs.auth.application.command;

public record ResetPasswordCommand(
        String token,
        String newPassword
) {
}
