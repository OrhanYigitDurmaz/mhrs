package com.mhrs.doctor.application.command;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateScheduleCommand(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer slotMinutes,
    String timezone
) {}
