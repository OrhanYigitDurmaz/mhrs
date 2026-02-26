package com.mhrs.appointment.api;

import com.mhrs.appointment.api.dto.AdminOverrideAppointmentRequest;
import com.mhrs.appointment.api.dto.AppointmentResponse;
import com.mhrs.appointment.api.dto.CancelAppointmentRequest;
import com.mhrs.appointment.api.dto.CreateAppointmentRequest;
import com.mhrs.appointment.api.dto.RescheduleAppointmentRequest;
import com.mhrs.appointment.application.AppointmentUseCase;
import com.mhrs.appointment.application.command.AdminOverrideAppointmentCommand;
import com.mhrs.appointment.application.command.CancelAppointmentCommand;
import com.mhrs.appointment.application.command.CreateAppointmentCommand;
import com.mhrs.appointment.application.command.RescheduleAppointmentCommand;
import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import com.mhrs.appointment.domain.AppointmentStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;

    public AppointmentController(AppointmentUseCase appointmentUseCase) {
        this.appointmentUseCase = appointmentUseCase;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
        @Valid @RequestBody CreateAppointmentRequest request
    ) {
        Appointment appointment = appointmentUseCase.createAppointment(
            new CreateAppointmentCommand(
                request.patientId(),
                request.doctorId(),
                request.clinicId(),
                request.departmentId(),
                request.specialtyId(),
                request.date(),
                request.startTime(),
                request.endTime(),
                request.reason(),
                request.notes()
            )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
            toResponse(appointment)
        );
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listAppointments(
        @RequestParam(required = false) String patientId,
        @RequestParam(required = false) String doctorId,
        @RequestParam(required = false) String clinicId,
        @RequestParam(required = false) String departmentId,
        @RequestParam(required = false) String specialtyId,
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate dateFrom,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate dateTo
    ) {
        AppointmentSearchQuery query = new AppointmentSearchQuery(
            patientId,
            doctorId,
            clinicId,
            departmentId,
            specialtyId,
            status,
            dateFrom,
            dateTo
        );
        List<AppointmentResponse> appointments = appointmentUseCase
            .listAppointments(query)
            .stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(
        @PathVariable String id
    ) {
        Appointment appointment = appointmentUseCase.getAppointment(id);
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
        @PathVariable String id
    ) {
        Appointment appointment = appointmentUseCase.confirmAppointment(id);
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
        @PathVariable String id,
        @Valid @RequestBody(required = false) CancelAppointmentRequest request
    ) {
        CancelAppointmentCommand command = new CancelAppointmentCommand(
            request != null ? request.reason() : null
        );
        Appointment appointment = appointmentUseCase.cancelAppointment(
            id,
            command
        );
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
        @PathVariable String id,
        @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        Appointment appointment = appointmentUseCase.rescheduleAppointment(
            id,
            new RescheduleAppointmentCommand(
                request.date(),
                request.startTime(),
                request.endTime(),
                request.reason()
            )
        );
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
        @PathVariable String id
    ) {
        Appointment appointment = appointmentUseCase.completeAppointment(id);
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShowAppointment(
        @PathVariable String id
    ) {
        Appointment appointment = appointmentUseCase.noShowAppointment(id);
        return ResponseEntity.ok(toResponse(appointment));
    }

    @PostMapping("/{id}/admin-override")
    public ResponseEntity<AppointmentResponse> adminOverride(
        @PathVariable String id,
        @Valid @RequestBody AdminOverrideAppointmentRequest request
    ) {
        Appointment appointment = appointmentUseCase.adminOverrideAppointment(
            id,
            new AdminOverrideAppointmentCommand(
                request.status(),
                request.notes()
            )
        );
        return ResponseEntity.ok(toResponse(appointment));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.appointmentId(),
            appointment.patientId(),
            appointment.doctorId(),
            appointment.clinicId(),
            appointment.departmentId(),
            appointment.specialtyId(),
            appointment.date(),
            appointment.startTime(),
            appointment.endTime(),
            appointment.status() != null ? appointment.status().name() : null,
            appointment.reason(),
            appointment.notes(),
            appointment.createdAt(),
            appointment.updatedAt()
        );
    }
}
