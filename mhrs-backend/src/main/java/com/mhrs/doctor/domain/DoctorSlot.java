package com.mhrs.doctor.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorSlot(
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    boolean available,
    String timezone
) {}
