package com.mhrs.doctor.domain;

import java.time.LocalDate;

public record DoctorLeave(
    String leaveId,
    LocalDate startDate,
    LocalDate endDate,
    String reason,
    boolean active
) {}
