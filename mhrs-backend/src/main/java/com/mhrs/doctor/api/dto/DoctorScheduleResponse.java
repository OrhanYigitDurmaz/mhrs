package com.mhrs.doctor.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record DoctorScheduleResponse(
    String scheduleId,
    DayOfWeek dayOfWeek,
    @JsonFormat(pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm") LocalTime endTime,
    int slotMinutes,
    String timezone,
    boolean active
) {}
