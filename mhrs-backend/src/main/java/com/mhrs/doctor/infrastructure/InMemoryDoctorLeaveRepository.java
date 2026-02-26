package com.mhrs.doctor.infrastructure;

import com.mhrs.doctor.application.port.out.DoctorLeaveRepository;
import com.mhrs.doctor.domain.DoctorLeave;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryDoctorLeaveRepository implements DoctorLeaveRepository {

    private final Map<String, Map<String, DoctorLeave>> store =
        new ConcurrentHashMap<>();

    @Override
    public DoctorLeave save(String doctorId, DoctorLeave leave) {
        store
            .computeIfAbsent(doctorId, key -> new ConcurrentHashMap<>())
            .put(leave.leaveId(), leave);
        return leave;
    }

    @Override
    public List<DoctorLeave> findByDoctorId(String doctorId) {
        Map<String, DoctorLeave> leaves = store.get(doctorId);
        if (leaves == null) {
            return List.of();
        }
        return new ArrayList<>(leaves.values());
    }

    @Override
    public Optional<DoctorLeave> findById(String doctorId, String leaveId) {
        Map<String, DoctorLeave> leaves = store.get(doctorId);
        if (leaves == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(leaves.get(leaveId));
    }

    @Override
    public DoctorLeave update(String doctorId, DoctorLeave leave) {
        Map<String, DoctorLeave> leaves = store.get(doctorId);
        if (leaves == null || !leaves.containsKey(leave.leaveId())) {
            throw new IllegalArgumentException(
                "Leave not found: " + leave.leaveId()
            );
        }
        leaves.put(leave.leaveId(), leave);
        return leave;
    }

    @Override
    public void delete(String doctorId, String leaveId) {
        Map<String, DoctorLeave> leaves = store.get(doctorId);
        if (leaves == null || !leaves.containsKey(leaveId)) {
            throw new IllegalArgumentException("Leave not found: " + leaveId);
        }
        leaves.remove(leaveId);
    }
}
