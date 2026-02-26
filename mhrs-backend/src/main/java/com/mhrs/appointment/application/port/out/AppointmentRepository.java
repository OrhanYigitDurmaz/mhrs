package com.mhrs.appointment.application.port.out;

import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);

    Optional<Appointment> findById(String appointmentId);

    List<Appointment> findAll(AppointmentSearchQuery query);
}
