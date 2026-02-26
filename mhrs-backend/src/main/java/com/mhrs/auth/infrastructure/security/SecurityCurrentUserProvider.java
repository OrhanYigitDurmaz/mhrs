package com.mhrs.auth.infrastructure.security;

import com.mhrs.auth.application.port.out.CurrentUserProvider;
import com.mhrs.auth.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public AuthenticatedUser currentUser() {
        AuthPrincipal principal = currentPrincipal();
        return new AuthenticatedUser(
            principal.userId(),
            principal.email(),
            principal.role(),
            principal.emailVerified()
        );
    }

    @Override
    public String currentSessionId() {
        return currentPrincipal().sessionId();
    }

    private AuthPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated request.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthPrincipal authPrincipal) {
            return authPrincipal;
        }
        throw new IllegalArgumentException("Unauthenticated request.");
    }
}
