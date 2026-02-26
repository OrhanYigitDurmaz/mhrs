package com.mhrs.doctor.api.dto;

import java.time.LocalDate;

public record DoctorLeaveResponse(
    String leaveId,
    LocalDate startDate,
    LocalDate endDate,
    String reason,
    boolean active
) {}
