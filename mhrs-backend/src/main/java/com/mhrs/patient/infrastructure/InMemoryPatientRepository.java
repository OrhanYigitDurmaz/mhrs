package com.mhrs.patient.infrastructure;

import com.mhrs.patient.application.port.out.PatientRepository;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.Patient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPatientRepository implements PatientRepository {

    private final Map<String, Patient> store = new ConcurrentHashMap<>();

    @Override
    public List<Patient> findAll(PatientSearchQuery query) {
        Stream<Patient> stream = store.values().stream();
        if (query != null) {
            if (query.status() != null) {
                stream = stream.filter(patient ->
                    query.status().equals(patient.status())
                );
            }
            if (query.query() != null && !query.query().isBlank()) {
                String q = query.query().toLowerCase(Locale.ROOT);
                stream = stream.filter(patient ->
                    containsIgnoreCase(patient.firstName(), q) ||
                    containsIgnoreCase(patient.lastName(), q) ||
                    containsIgnoreCase(patient.email(), q)
                );
            }
        }
        return stream.toList();
    }

    @Override
    public Optional<Patient> findById(String patientId) {
        return Optional.ofNullable(store.get(patientId));
    }

    @Override
    public Patient save(Patient patient) {
        store.put(patient.patientId(), patient);
        return patient;
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(query);
    }
}
