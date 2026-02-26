package com.mhrs.patient.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
    String patientId,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    String gender,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
