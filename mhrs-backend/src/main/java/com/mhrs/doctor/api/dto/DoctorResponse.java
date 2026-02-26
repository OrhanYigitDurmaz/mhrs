package com.mhrs.doctor.api.dto;

import java.time.Instant;

public record DoctorResponse(
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    String firstName,
    String lastName,
    String title,
    String email,
    String phone,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
