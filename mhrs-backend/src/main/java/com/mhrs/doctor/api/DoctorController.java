package com.mhrs.doctor.api;

import com.mhrs.doctor.api.dto.CreateDoctorRequest;
import com.mhrs.doctor.api.dto.CreateLeaveRequest;
import com.mhrs.doctor.api.dto.CreateScheduleRequest;
import com.mhrs.doctor.api.dto.DoctorLeaveResponse;
import com.mhrs.doctor.api.dto.DoctorResponse;
import com.mhrs.doctor.api.dto.DoctorScheduleResponse;
import com.mhrs.doctor.api.dto.DoctorSlotResponse;
import com.mhrs.doctor.api.dto.SetDoctorStatusRequest;
import com.mhrs.doctor.api.dto.UpdateDoctorRequest;
import com.mhrs.doctor.api.dto.UpdateLeaveRequest;
import com.mhrs.doctor.api.dto.UpdateScheduleRequest;
import com.mhrs.doctor.application.DoctorUseCase;
import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.CreateLeaveCommand;
import com.mhrs.doctor.application.command.CreateScheduleCommand;
import com.mhrs.doctor.application.command.SetDoctorStatusCommand;
import com.mhrs.doctor.application.command.UpdateDoctorCommand;
import com.mhrs.doctor.application.command.UpdateLeaveCommand;
import com.mhrs.doctor.application.command.UpdateScheduleCommand;
import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorLeave;
import com.mhrs.doctor.domain.DoctorSchedule;
import com.mhrs.doctor.domain.DoctorSlot;
import com.mhrs.doctor.domain.DoctorStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorUseCase doctorUseCase;

    public DoctorController(DoctorUseCase doctorUseCase) {
        this.doctorUseCase = doctorUseCase;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listDoctors(
        @RequestParam(required = false) String clinicId,
        @RequestParam(required = false) String departmentId,
        @RequestParam(required = false) String specialtyId,
        @RequestParam(required = false) DoctorStatus status
    ) {
        DoctorSearchQuery query = new DoctorSearchQuery(
            null,
            clinicId,
            departmentId,
            specialtyId,
            status
        );
        List<DoctorResponse> doctors = doctorUseCase
            .listDoctors(query)
            .stream()
            .map(this::toDoctorResponse)
            .toList();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponse> getDoctor(
        @PathVariable String doctorId
    ) {
        Doctor doctor = doctorUseCase.getDoctor(doctorId);
        return ResponseEntity.ok(toDoctorResponse(doctor));
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
        @Valid @RequestBody CreateDoctorRequest request
    ) {
        Doctor doctor = doctorUseCase.createDoctor(
            new CreateDoctorCommand(
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
            .body(toDoctorResponse(doctor));
    }

    @PatchMapping("/{doctorId}")
    public ResponseEntity<DoctorResponse> updateDoctor(
        @PathVariable String doctorId,
        @Valid @RequestBody UpdateDoctorRequest request
    ) {
        Doctor doctor = doctorUseCase.updateDoctor(
            doctorId,
            new UpdateDoctorCommand(
                request.clinicId(),
                request.departmentId(),
                request.specialtyId(),
                request.firstName(),
                request.lastName(),
                request.title(),
                request.email(),
                request.phone()
            )
        );
        return ResponseEntity.ok(toDoctorResponse(doctor));
    }

    @PatchMapping("/{doctorId}/set-status")
    public ResponseEntity<DoctorResponse> setStatus(
        @PathVariable String doctorId,
        @Valid @RequestBody SetDoctorStatusRequest request
    ) {
        Doctor doctor = doctorUseCase.setStatus(
            doctorId,
            new SetDoctorStatusCommand(request.status())
        );
        return ResponseEntity.ok(toDoctorResponse(doctor));
    }

    @PostMapping("/{id}/schedules")
    public ResponseEntity<DoctorScheduleResponse> createSchedule(
        @PathVariable("id") String doctorId,
        @Valid @RequestBody CreateScheduleRequest request
    ) {
        DoctorSchedule schedule = doctorUseCase.createSchedule(
            doctorId,
            new CreateScheduleCommand(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.slotMinutes(),
                request.timezone()
            )
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toScheduleResponse(schedule));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<DoctorScheduleResponse>> listSchedules(
        @PathVariable("id") String doctorId
    ) {
        List<DoctorScheduleResponse> schedules = doctorUseCase
            .listSchedules(doctorId)
            .stream()
            .map(this::toScheduleResponse)
            .toList();
        return ResponseEntity.ok(schedules);
    }

    @PatchMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<DoctorScheduleResponse> updateSchedule(
        @PathVariable("id") String doctorId,
        @PathVariable String scheduleId,
        @Valid @RequestBody UpdateScheduleRequest request
    ) {
        DoctorSchedule schedule = doctorUseCase.updateSchedule(
            doctorId,
            scheduleId,
            new UpdateScheduleCommand(
                request.startTime(),
                request.endTime(),
                request.slotMinutes(),
                request.timezone(),
                request.active()
            )
        );
        return ResponseEntity.ok(toScheduleResponse(schedule));
    }

    @DeleteMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
        @PathVariable("id") String doctorId,
        @PathVariable String scheduleId
    ) {
        doctorUseCase.deleteSchedule(doctorId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/leaves")
    public ResponseEntity<DoctorLeaveResponse> createLeave(
        @PathVariable("id") String doctorId,
        @Valid @RequestBody CreateLeaveRequest request
    ) {
        DoctorLeave leave = doctorUseCase.createLeave(
            doctorId,
            new CreateLeaveCommand(
                request.startDate(),
                request.endDate(),
                request.reason()
            )
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toLeaveResponse(leave));
    }

    @GetMapping("/{id}/leaves")
    public ResponseEntity<List<DoctorLeaveResponse>> listLeaves(
        @PathVariable("id") String doctorId
    ) {
        List<DoctorLeaveResponse> leaves = doctorUseCase
            .listLeaves(doctorId)
            .stream()
            .map(this::toLeaveResponse)
            .toList();
        return ResponseEntity.ok(leaves);
    }

    @PatchMapping("/{id}/leaves/{leaveId}")
    public ResponseEntity<DoctorLeaveResponse> updateLeave(
        @PathVariable("id") String doctorId,
        @PathVariable String leaveId,
        @Valid @RequestBody UpdateLeaveRequest request
    ) {
        DoctorLeave leave = doctorUseCase.updateLeave(
            doctorId,
            leaveId,
            new UpdateLeaveCommand(
                request.startDate(),
                request.endDate(),
                request.reason(),
                request.active()
            )
        );
        return ResponseEntity.ok(toLeaveResponse(leave));
    }

    @DeleteMapping("/{id}/leaves/{leaveId}")
    public ResponseEntity<Void> deleteLeave(
        @PathVariable("id") String doctorId,
        @PathVariable String leaveId
    ) {
        doctorUseCase.deleteLeave(doctorId, leaveId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<DoctorSlotResponse>> listSlots(
        @PathVariable("id") String doctorId,
        @RequestParam("date") @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate date
    ) {
        List<DoctorSlotResponse> slots = doctorUseCase
            .listSlots(doctorId, date)
            .stream()
            .map(this::toSlotResponse)
            .toList();
        return ResponseEntity.ok(slots);
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

    private DoctorScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
        return new DoctorScheduleResponse(
            schedule.scheduleId(),
            schedule.dayOfWeek(),
            schedule.startTime(),
            schedule.endTime(),
            schedule.slotMinutes(),
            schedule.timezone(),
            schedule.active()
        );
    }

    private DoctorLeaveResponse toLeaveResponse(DoctorLeave leave) {
        return new DoctorLeaveResponse(
            leave.leaveId(),
            leave.startDate(),
            leave.endDate(),
            leave.reason(),
            leave.active()
        );
    }

    private DoctorSlotResponse toSlotResponse(DoctorSlot slot) {
        return new DoctorSlotResponse(
            slot.date(),
            slot.startTime(),
            slot.endTime(),
            slot.available(),
            slot.timezone()
        );
    }
}
