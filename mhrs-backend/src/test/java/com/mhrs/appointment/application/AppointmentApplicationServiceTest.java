package com.mhrs.appointment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mhrs.appointment.application.command.AdminOverrideAppointmentCommand;
import com.mhrs.appointment.application.command.CancelAppointmentCommand;
import com.mhrs.appointment.application.command.CreateAppointmentCommand;
import com.mhrs.appointment.application.command.RescheduleAppointmentCommand;
import com.mhrs.appointment.application.port.out.AppointmentRepository;
import com.mhrs.appointment.domain.Appointment;
import com.mhrs.appointment.domain.AppointmentStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentApplicationServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private AppointmentApplicationService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
            Instant.parse("2026-01-01T00:00:00Z"),
            ZoneOffset.UTC
        );
        service = new AppointmentApplicationService(appointmentRepository, clock);
    }

    @Test
    void createAppointmentSavesAndReturnsAppointment() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Appointment created = service.createAppointment(
            new CreateAppointmentCommand(
                "p1",
                "d1",
                "c1",
                "dep1",
                "sp1",
                LocalDate.of(2026, 3, 10),
                LocalTime.of(9, 0),
                LocalTime.of(9, 20),
                "Routine checkup",
                "Bring previous reports"
            )
        );

        assertNotNull(created.appointmentId());
        assertEquals(AppointmentStatus.PENDING, created.status());
        assertEquals("p1", created.patientId());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void cancelAppointmentKeepsReasonAndAppendsNotes() {
        Appointment existing = mockAppointment();
        when(appointmentRepository.findById("a1")).thenReturn(
            Optional.of(existing)
        );
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Appointment cancelled = service.cancelAppointment(
            "a1",
            new CancelAppointmentCommand("Patient requested cancellation")
        );

        assertEquals(AppointmentStatus.CANCELLED, cancelled.status());
        assertEquals(existing.reason(), cancelled.reason());
        assertEquals(
            existing.notes() + "\nPatient requested cancellation",
            cancelled.notes()
        );
    }

    @Test
    void rescheduleAppointmentUpdatesDateAndTimeKeepsReason() {
        Appointment existing = mockAppointment();
        when(appointmentRepository.findById("a1")).thenReturn(
            Optional.of(existing)
        );
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Appointment rescheduled = service.rescheduleAppointment(
            "a1",
            new RescheduleAppointmentCommand(
                LocalDate.of(2026, 3, 12),
                LocalTime.of(10, 0),
                LocalTime.of(10, 20),
                "Scheduling conflict"
            )
        );

        assertEquals(AppointmentStatus.RESCHEDULED, rescheduled.status());
        assertEquals(LocalDate.of(2026, 3, 12), rescheduled.date());
        assertEquals(LocalTime.of(10, 0), rescheduled.startTime());
        assertEquals(LocalTime.of(10, 20), rescheduled.endTime());
        assertEquals(existing.reason(), rescheduled.reason());
        assertEquals(
            existing.notes() + "\nScheduling conflict",
            rescheduled.notes()
        );
    }

    @Test
    void adminOverrideMergesNotesAndUpdatesStatus() {
        Appointment existing = mockAppointment();
        when(appointmentRepository.findById("a1")).thenReturn(
            Optional.of(existing)
        );
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Appointment overridden = service.adminOverrideAppointment(
            "a1",
            new AdminOverrideAppointmentCommand(
                AppointmentStatus.CONFIRMED,
                "Admin override applied"
            )
        );

        assertEquals(AppointmentStatus.CONFIRMED, overridden.status());
        assertEquals(
            existing.notes() + "\nAdmin override applied",
            overridden.notes()
        );
    }

    private Appointment mockAppointment() {
        Instant now = Instant.now(clock);
        return new Appointment(
            "a1",
            "p1",
            "d1",
            "c1",
            "dep1",
            "sp1",
            LocalDate.of(2026, 3, 10),
            LocalTime.of(9, 0),
            LocalTime.of(9, 20),
            AppointmentStatus.PENDING,
            "Routine checkup",
            "Bring previous reports",
            now,
            now
        );
    }
}
