package com.mhrs.doctor.domain;

import java.time.LocalDate;

public final class LeaveRules {

    private LeaveRules() {}

    public static void validate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                "startDate must be on or before endDate"
            );
        }
    }
}
