package com.mhrs.doctor.application.port.out;

import com.mhrs.doctor.application.query.DoctorSearchQuery;
import com.mhrs.doctor.domain.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository {

    List<Doctor> findAll(DoctorSearchQuery query);

    Optional<Doctor> findById(String doctorId);

    Doctor save(Doctor doctor);
}
