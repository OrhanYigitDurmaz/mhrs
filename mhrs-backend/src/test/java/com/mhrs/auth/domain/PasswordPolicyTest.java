package com.mhrs.auth.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test
    void acceptsMinLength() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("12345678"));
    }

    @Test
    void rejectsNullOrTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
            PasswordPolicy.validate(null)
        );
        assertThrows(IllegalArgumentException.class, () ->
            PasswordPolicy.validate("1234567")
        );
    }
}
