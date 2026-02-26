package com.mhrs.doctor.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateDoctorRequest(
    @Size(min = 1) String clinicId,
    @Size(min = 1) String departmentId,
    @Size(min = 1) String specialtyId,
    @Size(min = 1) String firstName,
    @Size(min = 1) String lastName,
    @Size(min = 1) String title,
    @Email String email,
    @Size(min = 7, max = 20) String phone
) {}
