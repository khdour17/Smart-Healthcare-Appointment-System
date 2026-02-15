package org.example.healthcare.helpers;

import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Shared test data factory.
 * All test helpers use this to create consistent fake objects.
 * Keeps test data creation in ONE place — change here, updates everywhere.
 */
public class TestDataHelper {

    // ==================== USER ====================

    public static User createUser(Long id, String username, Role role) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@healthcare.com")
                .password("encoded-password")
                .role(role)
                .enabled(true)
                .build();
    }

    // ==================== DOCTOR ====================

    public static Doctor createDoctor(Long id, String name, String specialty) {
        return Doctor.builder()
                .id(id)
                .user(createUser(id, name.toLowerCase().replace(" ", "_"), Role.DOCTOR))
                .name(name)
                .specialty(specialty)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== PATIENT ====================

    public static Patient createPatient(Long id, String name) {
        return Patient.builder()
                .id(id)
                .user(createUser(id + 100, name.toLowerCase().replace(" ", "_"), Role.PATIENT))
                .name(name)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phone("555-0100")
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== AVAILABILITY ====================

    public static DoctorAvailability createAvailability(Long id, Doctor doctor, DayOfWeek day) {
        return DoctorAvailability.builder()
                .id(id)
                .doctor(doctor)
                .dayOfWeek(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .build();
    }

    // ==================== APPOINTMENT ====================

    public static Appointment createAppointment(Long id, Patient patient, Doctor doctor,
                                                LocalDate date, LocalTime start, LocalTime end) {
        return Appointment.builder()
                .id(id)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(start)
                .endTime(end)
                .status(AppointmentStatus.SCHEDULED)
                .reason("Checkup")
                .build();
    }
}