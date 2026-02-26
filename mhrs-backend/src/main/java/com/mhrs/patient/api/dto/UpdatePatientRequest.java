package com.mhrs.patient.api.dto;

import com.mhrs.patient.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePatientRequest(
    @Size(min = 1) String firstName,
    @Size(min = 1) String lastName,
    @Email String email,
    @Size(min = 7, max = 20) String phone,
    @PastOrPresent LocalDate dateOfBirth,
    Gender gender
) {}
