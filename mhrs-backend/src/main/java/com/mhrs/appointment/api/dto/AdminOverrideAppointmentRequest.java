package com.mhrs.appointment.api.dto;

import com.mhrs.appointment.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminOverrideAppointmentRequest(
    @NotNull AppointmentStatus status,
    @Size(max = 1000) String notes
) {}
