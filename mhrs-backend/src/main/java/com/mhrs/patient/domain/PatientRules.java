package com.mhrs.patient.domain;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class PatientRules {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
    );

    private PatientRules() {}

    public static void validateUpdate(
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        Gender gender
    ) {
        if (firstName != null && firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (lastName != null && lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email must be valid");
        }
        if (phone != null) {
            String trimmed = phone.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("phone must not be blank");
            }
            int length = trimmed.length();
            if (length < 7 || length > 20) {
                throw new IllegalArgumentException(
                    "phone length must be between 7 and 20"
                );
            }
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "dateOfBirth must not be in the future"
            );
        }
    }
}
