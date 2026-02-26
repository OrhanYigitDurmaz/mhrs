package com.mhrs.admin.application;

import com.mhrs.admin.application.command.AdminCreateDoctorCommand;
import com.mhrs.doctor.application.DoctorUseCase;
import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.SetDoctorStatusCommand;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminDoctorApplicationService implements AdminDoctorUseCase {

    private final DoctorUseCase doctorUseCase;

    public AdminDoctorApplicationService(DoctorUseCase doctorUseCase) {
        this.doctorUseCase = doctorUseCase;
    }

    @Override
    public Doctor createDoctor(AdminCreateDoctorCommand command) {
        return doctorUseCase.createDoctor(
            new CreateDoctorCommand(
                command.clinicId(),
                command.departmentId(),
                command.specialtyId(),
                command.firstName(),
                command.lastName(),
                command.title(),
                command.email(),
                command.phone(),
                command.status()
            )
        );
    }

    @Override
    public void removeDoctor(String doctorId) {
        doctorUseCase.setStatus(
            doctorId,
            new SetDoctorStatusCommand(DoctorStatus.INACTIVE)
        );
    }
}
