package org.example.healthcare.repository.sql;

import org.example.healthcare.models.sql.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{
}
