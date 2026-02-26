package com.mhrs.patient.infrastructure;

import com.mhrs.auth.infrastructure.security.AuthPrincipal;
import com.mhrs.patient.application.port.out.CurrentPatientProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentPatientProvider implements CurrentPatientProvider {

    @Override
    public String currentPatientId() {
        return currentPrincipal().userId();
    }

    @Override
    public String currentEmail() {
        return currentPrincipal().email();
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
