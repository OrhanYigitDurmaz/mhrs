package com.mhrs.doctor.infrastructure;

import com.mhrs.doctor.application.port.out.DoctorRepository;
import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryDoctorRepository implements DoctorRepository {

    private final Map<String, Doctor> store = new ConcurrentHashMap<>();

    @Override
    public List<Doctor> findAll(DoctorSearchQuery query) {
        Stream<Doctor> stream = store.values().stream();
        if (query != null) {
            if (query.clinicId() != null) {
                stream = stream.filter(doctor ->
                    query.clinicId().equals(doctor.clinicId())
                );
            }
            if (query.departmentId() != null) {
                stream = stream.filter(doctor ->
                    query.departmentId().equals(doctor.departmentId())
                );
            }
            if (query.specialtyId() != null) {
                stream = stream.filter(doctor ->
                    query.specialtyId().equals(doctor.specialtyId())
                );
            }
            if (query.status() != null) {
                stream = stream.filter(doctor ->
                    query.status().equals(doctor.status())
                );
            }
            if (query.query() != null && !query.query().isBlank()) {
                String q = query.query().toLowerCase(Locale.ROOT);
                stream = stream.filter(doctor ->
                    containsIgnoreCase(doctor.firstName(), q) ||
                    containsIgnoreCase(doctor.lastName(), q) ||
                    containsIgnoreCase(doctor.email(), q)
                );
            }
        }
        return stream.toList();
    }

    @Override
    public Optional<Doctor> findById(String doctorId) {
        return Optional.ofNullable(store.get(doctorId));
    }

    @Override
    public Doctor save(Doctor doctor) {
        store.put(doctor.doctorId(), doctor);
        return doctor;
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(query);
    }
}
