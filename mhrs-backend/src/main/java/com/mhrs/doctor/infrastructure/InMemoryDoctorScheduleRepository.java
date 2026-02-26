package com.mhrs.doctor.infrastructure;

import com.mhrs.doctor.application.port.out.DoctorScheduleRepository;
import com.mhrs.doctor.domain.DoctorSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryDoctorScheduleRepository
    implements DoctorScheduleRepository {

    private final Map<String, Map<String, DoctorSchedule>> store =
        new ConcurrentHashMap<>();

    @Override
    public DoctorSchedule save(String doctorId, DoctorSchedule schedule) {
        store
            .computeIfAbsent(doctorId, key -> new ConcurrentHashMap<>())
            .put(schedule.scheduleId(), schedule);
        return schedule;
    }

    @Override
    public List<DoctorSchedule> findByDoctorId(String doctorId) {
        Map<String, DoctorSchedule> schedules = store.get(doctorId);
        if (schedules == null) {
            return List.of();
        }
        return new ArrayList<>(schedules.values());
    }

    @Override
    public Optional<DoctorSchedule> findById(
        String doctorId,
        String scheduleId
    ) {
        Map<String, DoctorSchedule> schedules = store.get(doctorId);
        if (schedules == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(schedules.get(scheduleId));
    }

    @Override
    public DoctorSchedule update(String doctorId, DoctorSchedule schedule) {
        Map<String, DoctorSchedule> schedules = store.get(doctorId);
        if (schedules == null || !schedules.containsKey(schedule.scheduleId())) {
            throw new IllegalArgumentException(
                "Schedule not found: " + schedule.scheduleId()
            );
        }
        schedules.put(schedule.scheduleId(), schedule);
        return schedule;
    }

    @Override
    public void delete(String doctorId, String scheduleId) {
        Map<String, DoctorSchedule> schedules = store.get(doctorId);
        if (schedules == null || !schedules.containsKey(scheduleId)) {
            throw new IllegalArgumentException(
                "Schedule not found: " + scheduleId
            );
        }
        schedules.remove(scheduleId);
    }
}
