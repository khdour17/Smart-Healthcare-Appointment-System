package org.example.healthcare.service;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AdminMapper;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.*;
        import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AdminResponse getAdminById(Long id) {
        return adminRepository.findById(id)
                .map(adminMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    /**
     * Clears all data except the seeded admin account.
     * Order matters — delete children before parents (FK constraints).
     */
    @Transactional
    public void resetDatabase() {
        log.warn("[ADMIN] Database reset initiated");

        // 1. MongoDB (no FK constraints, safe to clear first)
        prescriptionRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        log.info("[ADMIN] MongoDB collections cleared");

        // 2. SQL — children first (FK order)
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        log.info("[ADMIN] SQL tables cleared (appointments, availability, doctors, patients)");

        // 3. Users — delete all except admin
        userRepository.deleteAllByRoleNot(Role.ADMIN);
        log.info("[ADMIN] Non-admin users deleted");

        log.warn("[ADMIN] Database reset complete");
    }
}