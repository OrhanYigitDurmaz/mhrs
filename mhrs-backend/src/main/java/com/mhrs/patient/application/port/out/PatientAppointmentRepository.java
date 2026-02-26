package com.mhrs.patient.application.port.out;

import com.mhrs.patient.domain.AppointmentSummary;
import java.util.List;

public interface PatientAppointmentRepository {

    List<AppointmentSummary> findByPatientId(String patientId);
}
