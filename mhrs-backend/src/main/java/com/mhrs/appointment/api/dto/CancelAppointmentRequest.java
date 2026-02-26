package com.mhrs.appointment.api.dto;

import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
    @Size(min = 1, max = 500) String reason
) {}
