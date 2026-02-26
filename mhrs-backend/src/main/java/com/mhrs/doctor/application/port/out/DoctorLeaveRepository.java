package com.mhrs.doctor.application.port.out;

import com.mhrs.doctor.domain.DoctorLeave;
import java.util.List;
import java.util.Optional;

public interface DoctorLeaveRepository {

    DoctorLeave save(String doctorId, DoctorLeave leave);

    List<DoctorLeave> findByDoctorId(String doctorId);

    Optional<DoctorLeave> findById(String doctorId, String leaveId);

    DoctorLeave update(String doctorId, DoctorLeave leave);

    void delete(String doctorId, String leaveId);
}
