package com.mhrs.doctor.domain;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ScheduleRules {

    private ScheduleRules() {}

    public static void validate(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotMinutes,
        String timezone
    ) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("dayOfWeek is required");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                "startTime and endTime required"
            );
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                "startTime must be before endTime"
            );
        }
        if (slotMinutes == null) {
            throw new IllegalArgumentException("slotMinutes is required");
        }
        if (slotMinutes < 5 || slotMinutes > 180) {
            throw new IllegalArgumentException(
                "slotMinutes must be between 5 and 180"
            );
        }
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("timezone is required");
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("timezone is invalid", ex);
        }
    }
}
