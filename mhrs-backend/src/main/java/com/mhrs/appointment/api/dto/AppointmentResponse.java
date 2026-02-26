package com.mhrs.appointment.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
    String appointmentId,
    String patientId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String reason,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
