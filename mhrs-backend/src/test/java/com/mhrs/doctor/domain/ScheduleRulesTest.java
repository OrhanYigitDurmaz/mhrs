package com.mhrs.doctor.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ScheduleRulesTest {

    @Test
    void validateAcceptsValidSchedule() {
        assertDoesNotThrow(() ->
            ScheduleRules.validate(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                20,
                "Europe/Istanbul"
            )
        );
    }

    @Test
    void validateRejectsInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () ->
            ScheduleRules.validate(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(9, 0),
                20,
                "Europe/Istanbul"
            )
        );
    }

    @Test
    void validateRejectsInvalidTimezone() {
        assertThrows(IllegalArgumentException.class, () ->
            ScheduleRules.validate(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                20,
                "Not/AZone"
            )
        );
    }
}
