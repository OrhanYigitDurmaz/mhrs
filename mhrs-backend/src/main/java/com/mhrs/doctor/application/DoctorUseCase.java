package com.mhrs.doctor.application;

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
import java.time.LocalDate;
import java.util.List;

public interface DoctorUseCase {

    List<Doctor> listDoctors(DoctorSearchQuery query);

    Doctor getDoctor(String doctorId);

    Doctor createDoctor(CreateDoctorCommand command);

    Doctor updateDoctor(String doctorId, UpdateDoctorCommand command);

    Doctor setStatus(String doctorId, SetDoctorStatusCommand command);

    DoctorSchedule createSchedule(String doctorId, CreateScheduleCommand command);

    List<DoctorSchedule> listSchedules(String doctorId);

    DoctorSchedule updateSchedule(
        String doctorId,
        String scheduleId,
        UpdateScheduleCommand command
    );

    void deleteSchedule(String doctorId, String scheduleId);

    DoctorLeave createLeave(String doctorId, CreateLeaveCommand command);

    List<DoctorLeave> listLeaves(String doctorId);

    DoctorLeave updateLeave(
        String doctorId,
        String leaveId,
        UpdateLeaveCommand command
    );

    void deleteLeave(String doctorId, String leaveId);

    List<DoctorSlot> listSlots(String doctorId, LocalDate date);

    List<Doctor> searchDoctors(DoctorSearchQuery query);
}
