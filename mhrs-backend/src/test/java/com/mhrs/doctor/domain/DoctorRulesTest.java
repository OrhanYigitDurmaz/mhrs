package com.mhrs.doctor.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DoctorRulesTest {

    @Test
    void validateCreateAcceptsValidDoctor() {
        assertDoesNotThrow(() ->
            DoctorRules.validateCreate(
                "clinic-1",
                "dept-1",
                "spec-1",
                "Aylin",
                "Kaya",
                "Dr.",
                "aylin@example.com",
                "+905001112233",
                DoctorStatus.ACTIVE
            )
        );
    }

    @Test
    void validateCreateRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            DoctorRules.validateCreate(
                "clinic-1",
                "dept-1",
                "spec-1",
                "Aylin",
                "Kaya",
                null,
                "invalid",
                null,
                DoctorStatus.ACTIVE
            )
        );
    }

    @Test
    void validateUpdateRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            DoctorRules.validateUpdate(
                null,
                null,
                null,
                " ",
                null,
                null,
                null,
                null
            )
        );
    }
}
