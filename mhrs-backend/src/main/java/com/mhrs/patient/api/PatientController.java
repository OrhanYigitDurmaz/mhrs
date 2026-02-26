package com.mhrs.patient.api;

import com.mhrs.patient.api.dto.AppointmentSummaryResponse;
import com.mhrs.patient.api.dto.PatientResponse;
import com.mhrs.patient.api.dto.SubmitVerificationRequest;
import com.mhrs.patient.api.dto.UpdatePatientRequest;
import com.mhrs.patient.api.dto.VerificationStatusResponse;
import com.mhrs.patient.application.PatientUseCase;
import com.mhrs.patient.application.command.SubmitVerificationCommand;
import com.mhrs.patient.application.command.UpdatePatientCommand;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.AppointmentSummary;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientStatus;
import com.mhrs.patient.domain.PatientVerification;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientUseCase patientUseCase;

    public PatientController(PatientUseCase patientUseCase) {
        this.patientUseCase = patientUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> me() {
        return ResponseEntity.ok(toPatientResponse(patientUseCase.getMe()));
    }

    @PatchMapping("/me")
    public ResponseEntity<PatientResponse> updateMe(
        @Valid @RequestBody UpdatePatientRequest request
    ) {
        Patient updated = patientUseCase.updateMe(
            new UpdatePatientCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.dateOfBirth(),
                request.gender()
            )
        );
        return ResponseEntity.ok(toPatientResponse(updated));
    }

    @GetMapping("/me/appointments")
    public ResponseEntity<List<AppointmentSummaryResponse>> myAppointments() {
        List<AppointmentSummaryResponse> appointments = patientUseCase
            .listMyAppointments()
            .stream()
            .map(this::toAppointmentResponse)
            .toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/me/verification-status")
    public ResponseEntity<VerificationStatusResponse> verificationStatus() {
        return ResponseEntity.ok(
            toVerificationResponse(patientUseCase.getVerificationStatus())
        );
    }

    @PostMapping("/me/verification/submit")
    public ResponseEntity<VerificationStatusResponse> submitVerification(
        @Valid @RequestBody SubmitVerificationRequest request
    ) {
        PatientVerification verification = patientUseCase.submitVerification(
            new SubmitVerificationCommand(
                request.identityNumber(),
                request.documentUrl(),
                request.notes()
            )
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toVerificationResponse(verification));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> listPatients(
        @RequestParam(required = false) PatientStatus status,
        @RequestParam(required = false) String query
    ) {
        PatientSearchQuery searchQuery = new PatientSearchQuery(query, status);
        List<PatientResponse> patients = patientUseCase
            .listPatients(searchQuery)
            .stream()
            .map(this::toPatientResponse)
            .toList();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatient(
        @PathVariable String patientId
    ) {
        Patient patient = patientUseCase.getPatient(patientId);
        return ResponseEntity.ok(toPatientResponse(patient));
    }

    @GetMapping("/{patientId}/appointments")
    public ResponseEntity<List<AppointmentSummaryResponse>> listAppointments(
        @PathVariable String patientId
    ) {
        List<AppointmentSummaryResponse> appointments = patientUseCase
            .listPatientAppointments(patientId)
            .stream()
            .map(this::toAppointmentResponse)
            .toList();
        return ResponseEntity.ok(appointments);
    }

    private PatientResponse toPatientResponse(Patient patient) {
        return new PatientResponse(
            patient.patientId(),
            patient.firstName(),
            patient.lastName(),
            patient.email(),
            patient.phone(),
            patient.dateOfBirth(),
            patient.gender() != null ? patient.gender().name() : null,
            patient.status() != null ? patient.status().name() : null,
            patient.createdAt(),
            patient.updatedAt()
        );
    }

    private AppointmentSummaryResponse toAppointmentResponse(
        AppointmentSummary summary
    ) {
        return new AppointmentSummaryResponse(
            summary.appointmentId(),
            summary.doctorId(),
            summary.clinicId(),
            summary.departmentId(),
            summary.specialtyId(),
            summary.scheduledAt(),
            summary.status()
        );
    }

    private VerificationStatusResponse toVerificationResponse(
        PatientVerification verification
    ) {
        return new VerificationStatusResponse(
            verification.status().name(),
            verification.submittedAt(),
            verification.reviewedAt(),
            verification.notes()
        );
    }
}
