package com.mhrs.admin.api;

import com.mhrs.admin.api.dto.AdminCreateDoctorRequest;
import com.mhrs.admin.api.dto.AdminDoctorResponse;
import com.mhrs.admin.application.AdminDoctorUseCase;
import com.mhrs.admin.application.command.AdminCreateDoctorCommand;
import com.mhrs.doctor.domain.Doctor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/doctors")
public class AdminDoctorController {

    private final AdminDoctorUseCase adminDoctorUseCase;

    public AdminDoctorController(AdminDoctorUseCase adminDoctorUseCase) {
        this.adminDoctorUseCase = adminDoctorUseCase;
    }

    @PostMapping
    public ResponseEntity<AdminDoctorResponse> createDoctor(
        @Valid @RequestBody AdminCreateDoctorRequest request
    ) {
        Doctor doctor = adminDoctorUseCase.createDoctor(
            new AdminCreateDoctorCommand(
                request.clinicId(),
                request.departmentId(),
                request.specialtyId(),
                request.firstName(),
                request.lastName(),
                request.title(),
                request.email(),
                request.phone(),
                request.status()
            )
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(doctor));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> removeDoctor(@PathVariable String doctorId) {
        adminDoctorUseCase.removeDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    private AdminDoctorResponse toResponse(Doctor doctor) {
        return new AdminDoctorResponse(
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
