package com.mhrs.doctor.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.UpdateDoctorCommand;
import com.mhrs.doctor.application.port.out.DoctorLeaveRepository;
import com.mhrs.doctor.application.port.out.DoctorRepository;
import com.mhrs.doctor.application.port.out.DoctorScheduleRepository;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorLeave;
import com.mhrs.doctor.domain.DoctorStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoctorApplicationServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @Mock
    private DoctorLeaveRepository leaveRepository;

    private DoctorApplicationService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"),
            ZoneOffset.UTC
        );
        service = new DoctorApplicationService(
            doctorRepository,
            scheduleRepository,
            leaveRepository,
            clock
        );
    }

    @Test
    void createDoctorSavesAndReturnsDoctor() {
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation ->
            invocation.getArgument(0)
        );

        Doctor doctor = service.createDoctor(
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

        assertNotNull(doctor.doctorId());
        assertEquals("clinic-1", doctor.clinicId());
        assertEquals(DoctorStatus.ACTIVE, doctor.status());
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void updateDoctorKeepsExistingFieldsWhenNull() {
        Doctor existing = mockDoctor();
        when(doctorRepository.findById("d1")).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation ->
            invocation.getArgument(0)
        );

        Doctor updated = service.updateDoctor(
            "d1",
            new UpdateDoctorCommand(
                null,
                null,
                null,
                null,
                null,
                "Prof.",
                null,
                null
            )
        );

        assertEquals("clinic-1", updated.clinicId());
        assertEquals("Prof.", updated.title());
    }

    @Test
    void listSlotsReturnsEmptyWhenOnLeave() {
        when(doctorRepository.findById("d1")).thenReturn(
            Optional.of(mockDoctor())
        );
        when(leaveRepository.findByDoctorId("d1")).thenReturn(
            List.of(
                new DoctorLeave(
                    "lv1",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 2),
                    null,
                    true
                )
            )
        );

        List<?> slots = service.listSlots("d1", LocalDate.of(2026, 1, 1));

        assertEquals(0, slots.size());
    }

    private Doctor mockDoctor() {
        Instant now = Instant.now(clock);
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
