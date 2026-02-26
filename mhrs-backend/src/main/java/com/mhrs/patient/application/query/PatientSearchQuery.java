package com.mhrs.patient.application.query;

import com.mhrs.patient.domain.PatientStatus;

public record PatientSearchQuery(String query, PatientStatus status) {}
