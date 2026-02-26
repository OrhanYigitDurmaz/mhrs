package com.mhrs.patient.api.dto;

import java.time.Instant;

public record VerificationStatusResponse(
    String status,
    Instant submittedAt,
    Instant reviewedAt,
    String notes
) {}
