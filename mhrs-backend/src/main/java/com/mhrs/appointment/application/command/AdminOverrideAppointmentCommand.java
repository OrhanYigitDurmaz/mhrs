package com.mhrs.appointment.application.command;

import com.mhrs.appointment.domain.AppointmentStatus;

public record AdminOverrideAppointmentCommand(
    AppointmentStatus status,
    String notes
) {}
