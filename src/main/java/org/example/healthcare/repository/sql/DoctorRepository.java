package org.example.healthcare.repository.sql;

import org.example.healthcare.models.sql.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
    List<Doctor> findByIsAvailable(Boolean isAvailable);
    Optional<Doctor> findByUserId(Long userId);
}
