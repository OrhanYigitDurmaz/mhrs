package com.mhrs.auth.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mhrs.auth")
public record AuthProperties(
        @NotNull Jwt jwt,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl,
        @NotNull Duration verificationTokenTtl,
        @NotNull Duration resetTokenTtl
) {

    public record Jwt(@NotBlank String secret) {
    }
}
