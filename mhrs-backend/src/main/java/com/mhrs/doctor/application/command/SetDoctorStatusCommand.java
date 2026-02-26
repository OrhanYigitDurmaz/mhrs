package com.mhrs.doctor.application.command;

import com.mhrs.doctor.domain.DoctorStatus;

public record SetDoctorStatusCommand(DoctorStatus status) {}
