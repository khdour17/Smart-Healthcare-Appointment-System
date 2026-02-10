package org.example.healthcare.repository.sql;

import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.models.sql.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status != 'CANCELLED' " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    Long countOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status != 'CANCELLED' " +
            "ORDER BY a.startTime")
    List<Appointment> findBookedAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );
}
