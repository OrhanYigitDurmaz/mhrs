package com.mhrs.doctor.application.command;

public record UpdateDoctorCommand(
    String clinicId,
    String departmentId,
    String specialtyId,
    String firstName,
    String lastName,
    String title,
    String email,
    String phone
) {}
