package com.mhrs.doctor.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DoctorSchedule(
    String scheduleId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    int slotMinutes,
    String timezone,
    boolean active
) {}
