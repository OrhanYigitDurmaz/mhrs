package com.mhrs.doctor.application;

import com.mhrs.doctor.application.command.CreateDoctorCommand;
import com.mhrs.doctor.application.command.CreateLeaveCommand;
import com.mhrs.doctor.application.command.CreateScheduleCommand;
import com.mhrs.doctor.application.command.SetDoctorStatusCommand;
import com.mhrs.doctor.application.command.UpdateDoctorCommand;
import com.mhrs.doctor.application.command.UpdateLeaveCommand;
import com.mhrs.doctor.application.command.UpdateScheduleCommand;
import com.mhrs.doctor.application.port.out.DoctorLeaveRepository;
import com.mhrs.doctor.application.port.out.DoctorRepository;
import com.mhrs.doctor.application.port.out.DoctorScheduleRepository;
import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import com.mhrs.doctor.domain.DoctorLeave;
import com.mhrs.doctor.domain.DoctorRules;
import com.mhrs.doctor.domain.DoctorSchedule;
import com.mhrs.doctor.domain.DoctorSlot;
import com.mhrs.doctor.domain.DoctorStatus;
import com.mhrs.doctor.domain.LeaveRules;
import com.mhrs.doctor.domain.ScheduleRules;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DoctorApplicationService implements DoctorUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorLeaveRepository leaveRepository;
    private final Clock clock;

    public DoctorApplicationService(
        DoctorRepository doctorRepository,
        DoctorScheduleRepository scheduleRepository,
        DoctorLeaveRepository leaveRepository,
        Clock clock
    ) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.leaveRepository = leaveRepository;
        this.clock = clock;
    }

    @Override
    public List<Doctor> listDoctors(DoctorSearchQuery query) {
        return doctorRepository.findAll(query);
    }

    @Override
    public Doctor getDoctor(String doctorId) {
        return requireDoctor(doctorId);
    }

    @Override
    public Doctor createDoctor(CreateDoctorCommand command) {
        DoctorRules.validateCreate(
            command.clinicId(),
            command.departmentId(),
            command.specialtyId(),
            command.firstName(),
            command.lastName(),
            command.title(),
            command.email(),
            command.phone(),
            command.status()
        );
        Instant now = Instant.now(clock);
        Doctor doctor = new Doctor(
            UUID.randomUUID().toString(),
            command.clinicId(),
            command.departmentId(),
            command.specialtyId(),
            command.firstName(),
            command.lastName(),
            command.title(),
            command.email(),
            command.phone(),
            command.status(),
            now,
            now
        );
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(String doctorId, UpdateDoctorCommand command) {
        Doctor existing = requireDoctor(doctorId);
        Doctor updated = new Doctor(
            existing.doctorId(),
            firstNonNull(command.clinicId(), existing.clinicId()),
            firstNonNull(command.departmentId(), existing.departmentId()),
            firstNonNull(command.specialtyId(), existing.specialtyId()),
            firstNonNull(command.firstName(), existing.firstName()),
            firstNonNull(command.lastName(), existing.lastName()),
            firstNonNull(command.title(), existing.title()),
            firstNonNull(command.email(), existing.email()),
            firstNonNull(command.phone(), existing.phone()),
            existing.status(),
            existing.createdAt(),
            Instant.now(clock)
        );
        DoctorRules.validateUpdate(
            updated.clinicId(),
            updated.departmentId(),
            updated.specialtyId(),
            updated.firstName(),
            updated.lastName(),
            updated.title(),
            updated.email(),
            updated.phone()
        );
        return doctorRepository.save(updated);
    }

    @Override
    public Doctor setStatus(String doctorId, SetDoctorStatusCommand command) {
        Doctor existing = requireDoctor(doctorId);
        DoctorStatus status = command.status();
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        Doctor updated = new Doctor(
            existing.doctorId(),
            existing.clinicId(),
            existing.departmentId(),
            existing.specialtyId(),
            existing.firstName(),
            existing.lastName(),
            existing.title(),
            existing.email(),
            existing.phone(),
            status,
            existing.createdAt(),
            Instant.now(clock)
        );
        return doctorRepository.save(updated);
    }

    @Override
    public DoctorSchedule createSchedule(
        String doctorId,
        CreateScheduleCommand command
    ) {
        requireDoctor(doctorId);
        ScheduleRules.validate(
            command.dayOfWeek(),
            command.startTime(),
            command.endTime(),
            command.slotMinutes(),
            command.timezone()
        );
        DoctorSchedule schedule = new DoctorSchedule(
            UUID.randomUUID().toString(),
            command.dayOfWeek(),
            command.startTime(),
            command.endTime(),
            command.slotMinutes(),
            command.timezone(),
            true
        );
        return scheduleRepository.save(doctorId, schedule);
    }

    @Override
    public List<DoctorSchedule> listSchedules(String doctorId) {
        requireDoctor(doctorId);
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Override
    public DoctorSchedule updateSchedule(
        String doctorId,
        String scheduleId,
        UpdateScheduleCommand command
    ) {
        requireDoctor(doctorId);
        DoctorSchedule existing = scheduleRepository
            .findById(doctorId, scheduleId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Schedule not found: " + scheduleId
                )
            );
        DoctorSchedule updated = new DoctorSchedule(
            existing.scheduleId(),
            existing.dayOfWeek(),
            firstNonNull(command.startTime(), existing.startTime()),
            firstNonNull(command.endTime(), existing.endTime()),
            firstNonNull(command.slotMinutes(), existing.slotMinutes()),
            firstNonNull(command.timezone(), existing.timezone()),
            firstNonNull(command.active(), existing.active())
        );
        ScheduleRules.validate(
            updated.dayOfWeek(),
            updated.startTime(),
            updated.endTime(),
            updated.slotMinutes(),
            updated.timezone()
        );
        return scheduleRepository.update(doctorId, updated);
    }

    @Override
    public void deleteSchedule(String doctorId, String scheduleId) {
        requireDoctor(doctorId);
        scheduleRepository.delete(doctorId, scheduleId);
    }

    @Override
    public DoctorLeave createLeave(String doctorId, CreateLeaveCommand command) {
        requireDoctor(doctorId);
        LeaveRules.validate(command.startDate(), command.endDate());
        DoctorLeave leave = new DoctorLeave(
            UUID.randomUUID().toString(),
            command.startDate(),
            command.endDate(),
            command.reason(),
            true
        );
        return leaveRepository.save(doctorId, leave);
    }

    @Override
    public List<DoctorLeave> listLeaves(String doctorId) {
        requireDoctor(doctorId);
        return leaveRepository.findByDoctorId(doctorId);
    }

    @Override
    public DoctorLeave updateLeave(
        String doctorId,
        String leaveId,
        UpdateLeaveCommand command
    ) {
        requireDoctor(doctorId);
        DoctorLeave existing = leaveRepository
            .findById(doctorId, leaveId)
            .orElseThrow(() ->
                new IllegalArgumentException("Leave not found: " + leaveId)
            );
        DoctorLeave updated = new DoctorLeave(
            existing.leaveId(),
            firstNonNull(command.startDate(), existing.startDate()),
            firstNonNull(command.endDate(), existing.endDate()),
            firstNonNull(command.reason(), existing.reason()),
            firstNonNull(command.active(), existing.active())
        );
        LeaveRules.validate(updated.startDate(), updated.endDate());
        return leaveRepository.update(doctorId, updated);
    }

    @Override
    public void deleteLeave(String doctorId, String leaveId) {
        requireDoctor(doctorId);
        leaveRepository.delete(doctorId, leaveId);
    }

    @Override
    public List<DoctorSlot> listSlots(String doctorId, LocalDate date) {
        requireDoctor(doctorId);
        List<DoctorLeave> leaves = leaveRepository.findByDoctorId(doctorId);
        boolean onLeave = leaves
            .stream()
            .anyMatch(leave ->
                leave.active() &&
                !date.isBefore(leave.startDate()) &&
                !date.isAfter(leave.endDate())
            );
        if (onLeave) {
            return List.of();
        }
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(
            doctorId
        );
        List<DoctorSlot> slots = new ArrayList<>();
        for (DoctorSchedule schedule : schedules) {
            if (!schedule.active()) {
                continue;
            }
            if (!schedule.dayOfWeek().equals(date.getDayOfWeek())) {
                continue;
            }
            LocalTime cursor = schedule.startTime();
            while (!cursor
                .plusMinutes(schedule.slotMinutes())
                .isAfter(schedule.endTime())) {
                LocalTime end = cursor.plusMinutes(schedule.slotMinutes());
                slots.add(
                    new DoctorSlot(
                        date,
                        cursor,
                        end,
                        true,
                        schedule.timezone()
                    )
                );
                cursor = end;
            }
        }
        return slots;
    }

    @Override
    public List<Doctor> searchDoctors(DoctorSearchQuery query) {
        return doctorRepository.findAll(query);
    }

    private Doctor requireDoctor(String doctorId) {
        return doctorRepository
            .findById(doctorId)
            .orElseThrow(() ->
                new IllegalArgumentException("Doctor not found: " + doctorId)
            );
    }

    private static <T> T firstNonNull(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
