package com.mhrs.appointment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentRequest(
    @NotBlank String patientId,
    @NotBlank String doctorId,
    @NotBlank String clinicId,
    @NotBlank String departmentId,
    @NotBlank String specialtyId,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Size(min = 1, max = 500) String reason,
    @Size(max = 1000) String notes
) {}
