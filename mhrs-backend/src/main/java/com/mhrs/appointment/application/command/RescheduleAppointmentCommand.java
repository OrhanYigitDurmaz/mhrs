package com.mhrs.appointment.application.command;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleAppointmentCommand(
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String reason
) {}
