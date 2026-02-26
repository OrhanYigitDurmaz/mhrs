package com.mhrs.patient.application.command;

import com.mhrs.patient.domain.Gender;
import java.time.LocalDate;

public record UpdatePatientCommand(
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    Gender gender
) {}
