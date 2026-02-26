package com.mhrs.doctor.api.dto;

import com.mhrs.doctor.domain.DoctorStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
    @NotBlank String clinicId,
    @NotBlank String departmentId,
    @NotBlank String specialtyId,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String title,
    @Email String email,
    @Size(min = 7, max = 20) String phone,
    @NotNull DoctorStatus status
) {}
