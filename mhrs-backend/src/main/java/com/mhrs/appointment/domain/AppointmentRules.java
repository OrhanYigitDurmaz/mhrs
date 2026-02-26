package com.mhrs.appointment.domain;

public final class AppointmentRules {

    private AppointmentRules() {}

    public static void validateCreate(
        String patientId,
        String doctorId,
        String clinicId,
        String departmentId,
        String specialtyId,
        java.time.LocalDate date,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime,
        String reason,
        String notes
    ) {
        requireNonBlank(patientId, "patientId");
        requireNonBlank(doctorId, "doctorId");
        requireNonBlank(clinicId, "clinicId");
        requireNonBlank(departmentId, "departmentId");
        requireNonBlank(specialtyId, "specialtyId");
        requireNonNull(date, "date");
        requireNonNull(startTime, "startTime");
        requireNonNull(endTime, "endTime");
        validateTimeRange(startTime, endTime);
        validateReason(reason);
        validateNotes(notes);
    }

    public static void validateReschedule(
        java.time.LocalDate date,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime,
        String reason
    ) {
        requireNonNull(date, "date");
        requireNonNull(startTime, "startTime");
        requireNonNull(endTime, "endTime");
        validateTimeRange(startTime, endTime);
        validateReason(reason);
    }

    public static void validateCancelReason(String reason) {
        validateReason(reason);
    }

    public static void validateAdminOverride(
        AppointmentStatus status,
        String notes
    ) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        validateNotes(notes);
    }

    private static void validateTimeRange(
        java.time.LocalTime startTime,
        java.time.LocalTime endTime
    ) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                "startTime must be before endTime"
            );
        }
    }

    private static void validateReason(String reason) {
        if (reason == null) {
            return;
        }
        String trimmed = reason.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("reason length must be <= 500");
        }
    }

    private static void validateNotes(String notes) {
        if (notes == null) {
            return;
        }
        if (notes.length() > 1000) {
            throw new IllegalArgumentException("notes length must be <= 1000");
        }
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
