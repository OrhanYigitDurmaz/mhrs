package com.mhrs.doctor.api.dto;

import com.mhrs.doctor.domain.DoctorStatus;
import jakarta.validation.constraints.NotNull;

public record SetDoctorStatusRequest(@NotNull DoctorStatus status) {}
