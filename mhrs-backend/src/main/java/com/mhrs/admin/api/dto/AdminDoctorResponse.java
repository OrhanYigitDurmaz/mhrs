package com.mhrs.admin.api.dto;

import java.time.Instant;

public record AdminDoctorResponse(
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
