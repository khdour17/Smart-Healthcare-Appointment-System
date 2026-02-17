package org.example.healthcare.service;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AdminMapper;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
        try {
            return adminRepository.findAll().stream()
                    .map(adminMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch all admins", ex);
        }
    }

    public AdminResponse getAdminById(Long id) {
        try {
            return adminRepository.findById(id)
                    .map(adminMapper::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch admin with id: " + id, ex);
        }
    }

    @Transactional
    public void resetDatabase() {
        log.warn("[ADMIN] Database reset initiated");

        try {
            prescriptionRepository.deleteAll();
            medicalRecordRepository.deleteAll();
            log.info("[ADMIN] MongoDB collections cleared");

            appointmentRepository.deleteAll();
            availabilityRepository.deleteAll();
            doctorRepository.deleteAll();
            patientRepository.deleteAll();
            log.info("[ADMIN] SQL tables cleared (appointments, availability, doctors, patients)");

            userRepository.deleteAllByRoleNot(Role.ADMIN);
            log.info("[ADMIN] Non-admin users deleted");

            log.warn("[ADMIN] Database reset complete");
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Database reset failed: " + ex.getMessage(), ex);
        }
    }
}