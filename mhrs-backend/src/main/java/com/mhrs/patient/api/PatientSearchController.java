package com.mhrs.patient.api;

import com.mhrs.patient.api.dto.PatientResponse;
import com.mhrs.patient.application.PatientUseCase;
import com.mhrs.patient.application.query.PatientSearchQuery;
import com.mhrs.patient.domain.Patient;
import com.mhrs.patient.domain.PatientStatus;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/patients")
public class PatientSearchController {

    private final PatientUseCase patientUseCase;

    public PatientSearchController(PatientUseCase patientUseCase) {
        this.patientUseCase = patientUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> searchPatients(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) PatientStatus status
    ) {
        PatientSearchQuery searchQuery = new PatientSearchQuery(query, status);
        List<PatientResponse> patients = patientUseCase
            .searchPatients(searchQuery)
            .stream()
            .map(this::toPatientResponse)
            .toList();
        return ResponseEntity.ok(patients);
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
}
