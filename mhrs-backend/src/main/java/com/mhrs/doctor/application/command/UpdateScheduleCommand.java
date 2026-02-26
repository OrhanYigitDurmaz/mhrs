package com.mhrs.doctor.application.command;

import java.time.LocalTime;

public record UpdateScheduleCommand(
    LocalTime startTime,
    LocalTime endTime,
    Integer slotMinutes,
    String timezone,
    Boolean active
) {}
