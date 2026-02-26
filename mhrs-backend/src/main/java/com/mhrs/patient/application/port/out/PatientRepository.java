package com.mhrs.patient.application.port.out;

import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {

    List<Patient> findAll(PatientSearchQuery query);

    Optional<Patient> findById(String patientId);

    Patient save(Patient patient);
}
