package com.mhrs.patient.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VerificationRulesTest {

    @Test
    void validateAcceptsValidInput() {
        assertDoesNotThrow(() ->
            VerificationRules.validate(
                "ABC12345",
                "https://example.com/documents/scan-1.pdf"
            )
        );
    }

    @Test
    void validateRejectsInvalidIdentityNumber() {
        assertThrows(IllegalArgumentException.class, () ->
            VerificationRules.validate("12", "https://example.com/doc.pdf")
        );
    }

    @Test
    void validateRejectsInvalidDocumentUrl() {
        assertThrows(IllegalArgumentException.class, () ->
            VerificationRules.validate("ABC12345", "not-a-url")
        );
    }
}
