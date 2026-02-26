package com.mhrs.doctor.application.query;

import com.mhrs.doctor.domain.DoctorStatus;

public record DoctorSearchQuery(
    String query,
    String clinicId,
    String departmentId,
    String specialtyId,
    DoctorStatus status
) {}
