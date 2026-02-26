package com.mhrs.patient.domain;

import java.time.Instant;
import java.time.LocalDate;

public record Patient(
    String patientId,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    Gender gender,
    PatientStatus status,
    Instant createdAt,
    Instant updatedAt
) {}
