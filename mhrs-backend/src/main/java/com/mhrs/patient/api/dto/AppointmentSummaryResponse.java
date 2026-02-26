package com.mhrs.patient.api.dto;

import java.time.Instant;

public record AppointmentSummaryResponse(
    String appointmentId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    Instant scheduledAt,
    String status
) {}
