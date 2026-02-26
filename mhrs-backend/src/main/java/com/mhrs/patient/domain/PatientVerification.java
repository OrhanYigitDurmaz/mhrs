package com.mhrs.patient.domain;

import java.time.Instant;

public record PatientVerification(
    VerificationStatus status,
    Instant submittedAt,
    Instant reviewedAt,
    String notes
) {}
