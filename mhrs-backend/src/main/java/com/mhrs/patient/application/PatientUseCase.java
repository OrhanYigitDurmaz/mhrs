package com.mhrs.patient.application;

import com.mhrs.patient.application.command.SubmitVerificationCommand;
import com.mhrs.patient.application.command.UpdatePatientCommand;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.AppointmentSummary;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientVerification;
import java.util.List;

public interface PatientUseCase {

    Patient getMe();

    Patient updateMe(UpdatePatientCommand command);

    List<AppointmentSummary> listMyAppointments();

    PatientVerification getVerificationStatus();

    PatientVerification submitVerification(SubmitVerificationCommand command);

    List<Patient> listPatients(PatientSearchQuery query);

    Patient getPatient(String patientId);

    List<AppointmentSummary> listPatientAppointments(String patientId);

    List<Patient> searchPatients(PatientSearchQuery query);
}
