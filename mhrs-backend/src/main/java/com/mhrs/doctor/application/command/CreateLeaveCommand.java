package com.mhrs.doctor.application.command;

import java.time.LocalDate;

public record CreateLeaveCommand(
    LocalDate startDate,
    LocalDate endDate,
    String reason
) {}
