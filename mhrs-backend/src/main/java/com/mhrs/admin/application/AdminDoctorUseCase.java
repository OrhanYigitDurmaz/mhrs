package com.mhrs.admin.application;

import com.mhrs.admin.application.command.AdminCreateDoctorCommand;
import com.mhrs.doctor.domain.Doctor;

public interface AdminDoctorUseCase {
    Doctor createDoctor(AdminCreateDoctorCommand command);

    void removeDoctor(String doctorId);
}
