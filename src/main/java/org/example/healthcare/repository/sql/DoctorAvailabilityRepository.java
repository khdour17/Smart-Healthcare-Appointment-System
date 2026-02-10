package org.example.healthcare.repository.sql;

import org.example.healthcare.models.sql.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorId(Long doctorId);
    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}