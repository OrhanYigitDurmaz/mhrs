package com.mhrs.patient.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitVerificationRequest(
    @NotBlank @Size(min = 6, max = 20) String identityNumber,
    @NotBlank String documentUrl,
    String notes
) {}
