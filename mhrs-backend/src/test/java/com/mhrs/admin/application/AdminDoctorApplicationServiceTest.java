package com.mhrs.admin.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mhrs.admin.application.command.AdminCreateDoctorCommand;
import com.mhrs.doctor.application.DoctorUseCase;
import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.SetDoctorStatusCommand;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDoctorApplicationServiceTest {

    @Mock
    private DoctorUseCase doctorUseCase;

    @Test
    void createDoctorDelegatesToDoctorUseCase() {
        AdminDoctorApplicationService service = new AdminDoctorApplicationService(
            doctorUseCase
        );
        AdminCreateDoctorCommand command = new AdminCreateDoctorCommand(
            "clinic-1",
            "dept-1",
            "spec-1",
            "Aylin",
            "Kaya",
            "Dr.",
            "aylin@example.com",
            "+905001112233",
            DoctorStatus.ACTIVE
        );
        when(
            doctorUseCase.createDoctor(
                new CreateDoctorCommand(
                    "clinic-1",
                    "dept-1",
                    "spec-1",
                    "Aylin",
                    "Kaya",
                    "Dr.",
                    "aylin@example.com",
                    "+905001112233",
                    DoctorStatus.ACTIVE
                )
            )
        ).thenReturn(mockDoctor());

        service.createDoctor(command);

        verify(doctorUseCase).createDoctor(
            new CreateDoctorCommand(
                "clinic-1",
                "dept-1",
                "spec-1",
                "Aylin",
                "Kaya",
                "Dr.",
                "aylin@example.com",
                "+905001112233",
                DoctorStatus.ACTIVE
            )
        );
    }

    @Test
    void removeDoctorSetsInactiveStatus() {
        AdminDoctorApplicationService service = new AdminDoctorApplicationService(
            doctorUseCase
        );

        service.removeDoctor("d1");

        verify(doctorUseCase).setStatus(
            "d1",
            new SetDoctorStatusCommand(DoctorStatus.INACTIVE)
        );
    }

    private Doctor mockDoctor() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Doctor(
            "d1",
            "clinic-1",
            "dept-1",
            "spec-1",
            "Aylin",
            "Kaya",
            "Dr.",
            "aylin@example.com",
            "+905001112233",
            DoctorStatus.ACTIVE,
            now,
            now
        );
    }
}
