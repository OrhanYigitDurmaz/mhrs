package com.mhrs.doctor.api.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateLeaveRequest(
    LocalDate startDate,
    LocalDate endDate,
    @Size(min = 1) String reason,
    Boolean active
) {}
