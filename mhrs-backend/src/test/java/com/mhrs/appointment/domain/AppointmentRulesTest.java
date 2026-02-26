package com.mhrs.appointment.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AppointmentRulesTest {

    @Test
    void validateCreateAcceptsValidInput() {
        assertDoesNotThrow(() ->
            AppointmentRules.validateCreate(
                "p1",
                "d1",
                "c1",
                "dep1",
                "sp1",
                LocalDate.of(2026, 3, 10),
                LocalTime.of(9, 0),
                LocalTime.of(9, 20),
                "Routine checkup",
                "Bring previous reports"
            )
        );
    }

    @Test
    void validateCreateRejectsMissingIds() {
        assertThrows(IllegalArgumentException.class, () ->
            AppointmentRules.validateCreate(
                " ",
                "d1",
                "c1",
                "dep1",
                "sp1",
                LocalDate.of(2026, 3, 10),
                LocalTime.of(9, 0),
                LocalTime.of(9, 20),
                "Reason",
                null
            )
        );
    }

    @Test
    void validateCreateRejectsInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () ->
            AppointmentRules.validateCreate(
                "p1",
                "d1",
                "c1",
                "dep1",
                "sp1",
                LocalDate.of(2026, 3, 10),
                LocalTime.of(10, 0),
                LocalTime.of(9, 0),
                "Reason",
                null
            )
        );
    }

    @Test
    void validateRescheduleRequiresDateAndTimes() {
        assertThrows(IllegalArgumentException.class, () ->
            AppointmentRules.validateReschedule(
                null,
                LocalTime.of(9, 0),
                LocalTime.of(9, 20),
                "Reason"
            )
        );
    }

    @Test
    void validateCancelReasonRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
            AppointmentRules.validateCancelReason("   ")
        );
    }

    @Test
    void validateAdminOverrideRequiresStatus() {
        assertThrows(IllegalArgumentException.class, () ->
            AppointmentRules.validateAdminOverride(null, "note")
        );
    }
}
