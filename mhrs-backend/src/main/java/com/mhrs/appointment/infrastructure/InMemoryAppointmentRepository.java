package com.mhrs.appointment.infrastructure;

import com.mhrs.appointment.application.port.out.AppointmentRepository;
import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final Map<String, Appointment> store = new ConcurrentHashMap<>();

    @Override
    public Appointment save(Appointment appointment) {
        store.put(appointment.appointmentId(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(String appointmentId) {
        return Optional.ofNullable(store.get(appointmentId));
    }

    @Override
    public List<Appointment> findAll(AppointmentSearchQuery query) {
        Stream<Appointment> stream = store.values().stream();
        if (query != null) {
            if (query.patientId() != null) {
                stream = stream.filter(appointment ->
                    query.patientId().equals(appointment.patientId())
                );
            }
            if (query.doctorId() != null) {
                stream = stream.filter(appointment ->
                    query.doctorId().equals(appointment.doctorId())
                );
            }
            if (query.clinicId() != null) {
                stream = stream.filter(appointment ->
                    query.clinicId().equals(appointment.clinicId())
                );
            }
            if (query.departmentId() != null) {
                stream = stream.filter(appointment ->
                    query.departmentId().equals(appointment.departmentId())
                );
            }
            if (query.specialtyId() != null) {
                stream = stream.filter(appointment ->
                    query.specialtyId().equals(appointment.specialtyId())
                );
            }
            if (query.status() != null) {
                stream = stream.filter(appointment ->
                    query.status().equals(appointment.status())
                );
            }
            if (query.dateFrom() != null) {
                LocalDate from = query.dateFrom();
                stream = stream.filter(appointment ->
                    appointment.date() != null && !appointment.date().isBefore(from)
                );
            }
            if (query.dateTo() != null) {
                LocalDate to = query.dateTo();
                stream = stream.filter(appointment ->
                    appointment.date() != null && !appointment.date().isAfter(to)
                );
            }
        }
        return stream.toList();
    }
}
