package org.example.healthcare.repository.sql;

import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.models.sql.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
}
