package com.mhrs.appointment.application.query;

import com.mhrs.appointment.domain.AppointmentStatus;
import java.time.LocalDate;

public record AppointmentSearchQuery(
    String patientId,
    String doctorId,
    String clinicId,
    String departmentId,
    String specialtyId,
    AppointmentStatus status,
    LocalDate dateFrom,
    LocalDate dateTo
) {}
