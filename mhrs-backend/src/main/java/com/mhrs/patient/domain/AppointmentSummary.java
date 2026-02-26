package com.mhrs.patient.domain;

import java.time.Instant;

public record AppointmentSummary(
    String appointmentId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    Instant scheduledAt,
    String status
) {}
