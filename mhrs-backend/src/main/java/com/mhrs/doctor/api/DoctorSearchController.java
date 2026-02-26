package com.mhrs.doctor.api;

import com.mhrs.doctor.api.dto.DoctorResponse;
import com.mhrs.doctor.application.DoctorUseCase;
import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorStatus;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/doctors")
public class DoctorSearchController {

    private final DoctorUseCase doctorUseCase;

    public DoctorSearchController(DoctorUseCase doctorUseCase) {
        this.doctorUseCase = doctorUseCase;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> searchDoctors(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String clinicId,
        @RequestParam(required = false) String departmentId,
        @RequestParam(required = false) String specialtyId,
        @RequestParam(required = false) DoctorStatus status
    ) {
        DoctorSearchQuery doctorQuery = new DoctorSearchQuery(
            query,
            clinicId,
            departmentId,
            specialtyId,
            status
        );
        List<DoctorResponse> results = doctorUseCase
            .searchDoctors(doctorQuery)
            .stream()
            .map(this::toDoctorResponse)
            .toList();
        return ResponseEntity.ok(results);
    }

    private DoctorResponse toDoctorResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.doctorId(),
            doctor.clinicId(),
            doctor.departmentId(),
            doctor.specialtyId(),
            doctor.firstName(),
            doctor.lastName(),
            doctor.title(),
            doctor.email(),
            doctor.phone(),
            doctor.status().name(),
            doctor.createdAt(),
            doctor.updatedAt()
        );
    }
}
