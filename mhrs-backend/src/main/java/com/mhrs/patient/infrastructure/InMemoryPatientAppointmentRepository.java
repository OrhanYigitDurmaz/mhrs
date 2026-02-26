package com.mhrs.patient.infrastructure;

import com.mhrs.patient.application.port.out.PatientAppointmentRepository;
import com.mhrs.patient.domain.AppointmentSummary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPatientAppointmentRepository
    implements PatientAppointmentRepository {

    private final Map<String, List<AppointmentSummary>> store =
        new ConcurrentHashMap<>();

    @Override
    public List<AppointmentSummary> findByPatientId(String patientId) {
        return store.getOrDefault(patientId, List.of());
    }
}
