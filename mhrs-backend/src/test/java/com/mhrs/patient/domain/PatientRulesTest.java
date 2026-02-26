package com.mhrs.patient.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PatientRulesTest {

    @Test
    void validateUpdateAcceptsValidData() {
        assertDoesNotThrow(() ->
            PatientRules.validateUpdate(
                "Elif",
                "Yilmaz",
                "elif@example.com",
                "+905001112233",
                LocalDate.of(1990, 5, 10),
                Gender.FEMALE
            )
        );
    }

    @Test
    void validateUpdateRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            PatientRules.validateUpdate(
                "Elif",
                "Yilmaz",
                "invalid",
                null,
                null,
                null
            )
        );
    }

    @Test
    void validateUpdateRejectsFutureDob() {
        assertThrows(IllegalArgumentException.class, () ->
            PatientRules.validateUpdate(
                null,
                null,
                null,
                null,
                LocalDate.now().plusDays(1),
                null
            )
        );
    }
}
