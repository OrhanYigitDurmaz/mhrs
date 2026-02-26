package com.mhrs.patient.application.command;

public record SubmitVerificationCommand(
    String identityNumber,
    String documentUrl,
    String notes
) {}
