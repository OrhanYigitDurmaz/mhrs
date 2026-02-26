package com.mhrs.doctor.application.port.out;

import com.mhrs.doctor.domain.DoctorSchedule;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository {

    DoctorSchedule save(String doctorId, DoctorSchedule schedule);

    List<DoctorSchedule> findByDoctorId(String doctorId);

    Optional<DoctorSchedule> findById(String doctorId, String scheduleId);

    DoctorSchedule update(String doctorId, DoctorSchedule schedule);

    void delete(String doctorId, String scheduleId);
}
