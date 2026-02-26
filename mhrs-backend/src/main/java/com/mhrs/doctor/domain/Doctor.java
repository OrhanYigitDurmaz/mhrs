package com.mhrs.doctor.domain;

import java.time.Instant;

public record Doctor(
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    String firstName,
    String lastName,
    String title,
    String email,
    String phone,
    DoctorStatus status,
    Instant createdAt,
    Instant updatedAt
) {}
