package com.mhrs.doctor.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateScheduleRequest(
    @JsonFormat(pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm") LocalTime endTime,
    @Min(5) @Max(180) Integer slotMinutes,
    @Size(min = 1) String timezone,
    Boolean active
) {}
