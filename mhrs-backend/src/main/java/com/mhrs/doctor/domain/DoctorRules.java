package com.mhrs.doctor.domain;

import java.util.regex.Pattern;

public final class DoctorRules {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
    );

    private DoctorRules() {}

    public static void validateCreate(
        String clinicId,
        String departmentId,
        String specialtyId,
        String firstName,
        String lastName,
        String title,
        String email,
        String phone,
        DoctorStatus status
    ) {
        requireNonBlank(clinicId, "clinicId");
        requireNonBlank(departmentId, "departmentId");
        requireNonBlank(specialtyId, "specialtyId");
        requireNonBlank(firstName, "firstName");
        requireNonBlank(lastName, "lastName");
        if (title != null) {
            requireNonBlank(title, "title");
        }
        validateEmail(email);
        validatePhone(phone);
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    public static void validateUpdate(
        String clinicId,
        String departmentId,
        String specialtyId,
        String firstName,
        String lastName,
        String title,
        String email,
        String phone
    ) {
        if (clinicId != null) {
            requireNonBlank(clinicId, "clinicId");
        }
        if (departmentId != null) {
            requireNonBlank(departmentId, "departmentId");
        }
        if (specialtyId != null) {
            requireNonBlank(specialtyId, "specialtyId");
        }
        if (firstName != null) {
            requireNonBlank(firstName, "firstName");
        }
        if (lastName != null) {
            requireNonBlank(lastName, "lastName");
        }
        if (title != null) {
            requireNonBlank(title, "title");
        }
        validateEmail(email);
        validatePhone(phone);
    }

    private static void validateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email must be valid");
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null) {
            return;
        }
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

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
