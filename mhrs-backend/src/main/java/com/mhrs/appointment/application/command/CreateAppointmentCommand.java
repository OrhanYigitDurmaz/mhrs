package com.mhrs.appointment.application.command;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentCommand(
    String patientId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String reason,
    String notes
) {}
