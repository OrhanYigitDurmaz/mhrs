package com.mhrs.doctor.application.command;

import java.time.LocalDate;

public record UpdateLeaveCommand(
    LocalDate startDate,
    LocalDate endDate,
    String reason,
    Boolean active
) {}
