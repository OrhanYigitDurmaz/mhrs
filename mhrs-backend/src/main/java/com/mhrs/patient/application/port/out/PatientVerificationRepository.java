package com.mhrs.patient.application.port.out;

import com.mhrs.patient.domain.PatientVerification;
import java.util.Optional;

public interface PatientVerificationRepository {

    Optional<PatientVerification> findByPatientId(String patientId);

    PatientVerification save(String patientId, PatientVerification verification);
}
