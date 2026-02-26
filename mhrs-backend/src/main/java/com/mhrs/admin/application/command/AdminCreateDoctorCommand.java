package com.mhrs.admin.application.command;

import com.mhrs.doctor.domain.DoctorStatus;

public record AdminCreateDoctorCommand(
    String clinicId,
    String departmentId,
    String specialtyId,
    String firstName,
    String lastName,
    String title,
    String email,
    String phone,
    DoctorStatus status
) {}
