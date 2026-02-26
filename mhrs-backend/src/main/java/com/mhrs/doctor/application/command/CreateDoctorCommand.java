package com.mhrs.doctor.application.command;

import com.mhrs.doctor.domain.DoctorStatus;

public record CreateDoctorCommand(
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
