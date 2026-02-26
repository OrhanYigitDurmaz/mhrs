package com.mhrs.patient.application;

import com.mhrs.patient.application.command.SubmitVerificationCommand;
import com.mhrs.patient.application.command.UpdatePatientCommand;
import com.mhrs.patient.application.port.out.CurrentPatientProvider;
import com.mhrs.patient.application.port.out.PatientAppointmentRepository;
import com.mhrs.patient.application.port.out.PatientRepository;
import com.mhrs.patient.application.port.out.PatientVerificationRepository;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.AppointmentSummary;
import com.mhrs.patient.domain.Gender;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientRules;
import com.mhrs.patient.domain.PatientStatus;
import com.mhrs.patient.domain.PatientVerification;
import com.mhrs.patient.domain.VerificationRules;
import com.mhrs.patient.domain.VerificationStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PatientApplicationService implements PatientUseCase {

    private final PatientRepository patientRepository;
    private final PatientVerificationRepository verificationRepository;
    private final PatientAppointmentRepository appointmentRepository;
    private final CurrentPatientProvider currentPatientProvider;
    private final Clock clock;

    public PatientApplicationService(
        PatientRepository patientRepository,
        PatientVerificationRepository verificationRepository,
        PatientAppointmentRepository appointmentRepository,
        CurrentPatientProvider currentPatientProvider,
        Clock clock
    ) {
        this.patientRepository = patientRepository;
        this.verificationRepository = verificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.currentPatientProvider = currentPatientProvider;
        this.clock = clock;
    }

    @Override
    public Patient getMe() {
        return getOrCreateCurrentPatient();
    }

    @Override
    public Patient updateMe(UpdatePatientCommand command) {
        Patient existing = getOrCreateCurrentPatient();
        Patient updated = new Patient(
            existing.patientId(),
            firstNonNull(command.firstName(), existing.firstName()),
            firstNonNull(command.lastName(), existing.lastName()),
            firstNonNull(command.email(), existing.email()),
            firstNonNull(command.phone(), existing.phone()),
            firstNonNull(command.dateOfBirth(), existing.dateOfBirth()),
            firstNonNull(command.gender(), existing.gender()),
            existing.status(),
            existing.createdAt(),
            Instant.now(clock)
        );
        PatientRules.validateUpdate(
            updated.firstName(),
            updated.lastName(),
            updated.email(),
            updated.phone(),
            updated.dateOfBirth(),
            updated.gender()
        );
        return patientRepository.save(updated);
    }

    @Override
    public List<AppointmentSummary> listMyAppointments() {
        String patientId = currentPatientProvider.currentPatientId();
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public PatientVerification getVerificationStatus() {
        String patientId = currentPatientProvider.currentPatientId();
        return verificationRepository
            .findByPatientId(patientId)
            .orElse(
                new PatientVerification(
                    VerificationStatus.UNVERIFIED,
                    null,
                    null,
                    null
                )
            );
    }

    @Override
    public PatientVerification submitVerification(
        SubmitVerificationCommand command
    ) {
        String patientId = currentPatientProvider.currentPatientId();
        VerificationRules.validate(
            command.identityNumber(),
            command.documentUrl()
        );
        PatientVerification verification = new PatientVerification(
            VerificationStatus.PENDING,
            Instant.now(clock),
            null,
            command.notes()
        );
        return verificationRepository.save(patientId, verification);
    }

    @Override
    public List<Patient> listPatients(PatientSearchQuery query) {
        return patientRepository.findAll(query);
    }

    @Override
    public Patient getPatient(String patientId) {
        return patientRepository
            .findById(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException("Patient not found: " + patientId)
            );
    }

    @Override
    public List<AppointmentSummary> listPatientAppointments(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Patient> searchPatients(PatientSearchQuery query) {
        return patientRepository.findAll(query);
    }

    private Patient getOrCreateCurrentPatient() {
        String patientId = currentPatientProvider.currentPatientId();
        return patientRepository
            .findById(patientId)
            .orElseGet(() -> createStubPatient(patientId));
    }

    private Patient createStubPatient(String patientId) {
        Instant now = Instant.now(clock);
        Patient patient = new Patient(
            patientId,
            null,
            null,
            currentPatientProvider.currentEmail(),
            null,
            null,
            Gender.UNKNOWN,
            PatientStatus.ACTIVE,
            now,
            now
        );
        return patientRepository.save(patient);
    }

    private static <T> T firstNonNull(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
