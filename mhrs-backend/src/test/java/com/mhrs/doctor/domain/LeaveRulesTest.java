package com.mhrs.doctor.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LeaveRulesTest {

    @Test
    void validateAcceptsValidRange() {
        assertDoesNotThrow(() ->
            LeaveRules.validate(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2)
            )
        );
    }

    @Test
    void validateRejectsStartAfterEnd() {
        assertThrows(IllegalArgumentException.class, () ->
            LeaveRules.validate(
                LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 3, 2)
            )
        );
    }
}
