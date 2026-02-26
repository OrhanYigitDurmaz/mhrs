package com.mhrs.auth.application.command;

public record LoginCommand(
        String email,
        String password
) {
}
