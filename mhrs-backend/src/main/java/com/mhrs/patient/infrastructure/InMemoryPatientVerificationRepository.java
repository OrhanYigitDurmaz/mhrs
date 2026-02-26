package com.mhrs.patient.infrastructure;

import com.mhrs.patient.application.port.out.PatientVerificationRepository;
import com.mhrs.patient.domain.PatientVerification;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPatientVerificationRepository
    implements PatientVerificationRepository {

    private final Map<String, PatientVerification> store =
        new ConcurrentHashMap<>();

    @Override
    public Optional<PatientVerification> findByPatientId(String patientId) {
        return Optional.ofNullable(store.get(patientId));
    }

    @Override
    public PatientVerification save(
        String patientId,
        PatientVerification verification
    ) {
        store.put(patientId, verification);
        return verification;
    }
}
