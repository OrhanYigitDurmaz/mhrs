package com.mhrs.appointment.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record Appointment(
    String appointmentId,
    String patientId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    AppointmentStatus status,
    String reason,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
