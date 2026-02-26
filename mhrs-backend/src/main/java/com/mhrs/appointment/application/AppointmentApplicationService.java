package com.mhrs.appointment.application;

import com.mhrs.appointment.application.command.AdminOverrideAppointmentCommand;
import com.mhrs.appointment.application.command.CancelAppointmentCommand;
import com.mhrs.appointment.application.command.CreateAppointmentCommand;
import com.mhrs.appointment.application.command.RescheduleAppointmentCommand;
import com.mhrs.appointment.application.port.out.AppointmentRepository;
import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import com.mhrs.appointment.domain.AppointmentRules;
import com.mhrs.appointment.domain.AppointmentStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AppointmentApplicationService implements AppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public AppointmentApplicationService(
        AppointmentRepository appointmentRepository,
        Clock clock
    ) {
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Override
    public Appointment createAppointment(CreateAppointmentCommand command) {
        AppointmentRules.validateCreate(
            command.patientId(),
            command.doctorId(),
            command.clinicId(),
            command.departmentId(),
            command.specialtyId(),
            command.date(),
            command.startTime(),
            command.endTime(),
            command.reason(),
            command.notes()
        );
        Instant now = Instant.now(clock);
        Appointment appointment = new Appointment(
            UUID.randomUUID().toString(),
            command.patientId(),
            command.doctorId(),
            command.clinicId(),
            command.departmentId(),
            command.specialtyId(),
            command.date(),
            command.startTime(),
            command.endTime(),
            AppointmentStatus.PENDING,
            command.reason(),
            command.notes(),
            now,
            now
        );
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> listAppointments(AppointmentSearchQuery query) {
        return appointmentRepository.findAll(query);
    }

    @Override
    public Appointment getAppointment(String appointmentId) {
        return requireAppointment(appointmentId);
    }

    @Override
    public Appointment confirmAppointment(String appointmentId) {
        Appointment existing = requireAppointment(appointmentId);
        return updateAppointment(
            existing,
            AppointmentStatus.CONFIRMED,
            existing.date(),
            existing.startTime(),
            existing.endTime(),
            existing.reason(),
            existing.notes()
        );
    }

    @Override
    public Appointment cancelAppointment(
        String appointmentId,
        CancelAppointmentCommand command
    ) {
        Appointment existing = requireAppointment(appointmentId);
        AppointmentRules.validateCancelReason(command.reason());
        String reason = existing.reason();
        String notes = mergeNotes(existing.notes(), command.reason());
        return updateAppointment(
            existing,
            AppointmentStatus.CANCELLED,
            existing.date(),
            existing.startTime(),
            existing.endTime(),
            reason,
            notes
        );
    }

    @Override
    public Appointment rescheduleAppointment(
        String appointmentId,
        RescheduleAppointmentCommand command
    ) {
        Appointment existing = requireAppointment(appointmentId);
        AppointmentRules.validateReschedule(
            command.date(),
            command.startTime(),
            command.endTime(),
            command.reason()
        );
        String reason = existing.reason();
        String notes = mergeNotes(existing.notes(), command.reason());
        return updateAppointment(
            existing,
            AppointmentStatus.RESCHEDULED,
            command.date(),
            command.startTime(),
            command.endTime(),
            reason,
            notes
        );
    }

    @Override
    public Appointment completeAppointment(String appointmentId) {
        Appointment existing = requireAppointment(appointmentId);
        return updateAppointment(
            existing,
            AppointmentStatus.COMPLETED,
            existing.date(),
            existing.startTime(),
            existing.endTime(),
            existing.reason(),
            existing.notes()
        );
    }

    @Override
    public Appointment noShowAppointment(String appointmentId) {
        Appointment existing = requireAppointment(appointmentId);
        return updateAppointment(
            existing,
            AppointmentStatus.NO_SHOW,
            existing.date(),
            existing.startTime(),
            existing.endTime(),
            existing.reason(),
            existing.notes()
        );
    }

    @Override
    public Appointment adminOverrideAppointment(
        String appointmentId,
        AdminOverrideAppointmentCommand command
    ) {
        Appointment existing = requireAppointment(appointmentId);
        AppointmentRules.validateAdminOverride(
            command.status(),
            command.notes()
        );
        String notes = mergeNotes(existing.notes(), command.notes());
        return updateAppointment(
            existing,
            command.status(),
            existing.date(),
            existing.startTime(),
            existing.endTime(),
            existing.reason(),
            notes
        );
    }

    private Appointment updateAppointment(
        Appointment existing,
        AppointmentStatus status,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        String notes
    ) {
        Appointment updated = new Appointment(
            existing.appointmentId(),
            existing.patientId(),
            existing.doctorId(),
            existing.clinicId(),
            existing.departmentId(),
            existing.specialtyId(),
            date,
            startTime,
            endTime,
            status,
            reason,
            notes,
            existing.createdAt(),
            Instant.now(clock)
        );
        return appointmentRepository.save(updated);
    }

    private Appointment requireAppointment(String appointmentId) {
        return appointmentRepository
            .findById(appointmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Appointment not found: " + appointmentId
                )
            );
    }

    private String mergeNotes(String existing, String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return existing;
        }
        if (existing == null || existing.isBlank()) {
            return incoming;
        }
        return existing + "\n" + incoming;
    }
}
