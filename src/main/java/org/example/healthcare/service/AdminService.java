package org.example.healthcare.service;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ForbiddenOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AdminMapper;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.Admin;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.*;
import org.example.healthcare.security.CallerGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
    private final CallerGuard callerGuard;

    @PersistenceContext
    private EntityManager entityManager;

    // Tables whose rows the reset clears — their AUTO_INCREMENT counters restart afterwards.
    // "users" keeps the admin row, so MySQL clamps its counter to MAX(id) + 1 rather than 1.
    private static final List<String> RESET_TABLES =
            List.of("appointments", "doctor_availability", "doctors", "patients", "users");

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
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        User user = admin.getUser();

        if (user != null && user.getId().equals(callerGuard.currentUserId())) {
            throw new ForbiddenOperationException("You can not delete your own admin account");
        }

        if (adminRepository.count() <= 1) {
            throw new ForbiddenOperationException("The last admin account can not be deleted");
        }

        try {
            adminRepository.delete(admin);
            // Flush so the admin row is gone before its users row is removed (FK: admins.user_id -> users.id)
            adminRepository.flush();
            if (user != null) {
                userRepository.delete(user);
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete admin with id: " + id, ex);
        }
    }

    @Transactional
    public void deleteAdmins(List<Long> ids) {
        List<Admin> adminsToDelete = adminRepository.findAllById(ids);
        if (adminsToDelete.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more admins not found for the given ids");
        }

        List<User> usersToDelete = adminsToDelete.stream()
                .map(Admin::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (usersToDelete.stream().anyMatch(user -> user.getId().equals(callerGuard.currentUserId()))) {
            throw new ForbiddenOperationException("You can not delete your own admin account");
        }

        if (adminRepository.count() - adminsToDelete.size() < 1) {
            throw new ForbiddenOperationException("The last admin account can not be deleted");
        }

        try {
            adminRepository.deleteAll(adminsToDelete);
            // Flush so the admin rows are gone before their users rows are removed (FK: admins.user_id -> users.id)
            adminRepository.flush();
            userRepository.deleteAll(usersToDelete);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete admins with ids: " + ids, ex);
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

            resetAutoIncrementCounters();
            log.info("[ADMIN] AUTO_INCREMENT counters reset");

            log.warn("[ADMIN] Database reset complete");
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Database reset failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Restarts the id counters so a reset database numbers new rows from 1 again.
     * DELETE leaves MySQL's AUTO_INCREMENT untouched, so without this the next
     * doctor/patient continues from the highest id ever used.
     * <p>
     * Deletes are flushed first so the rows are actually gone before the counters move.
     * Note: ALTER TABLE causes an implicit commit in MySQL, so the reset is not atomic
     * past this point — acceptable for an admin-only reset endpoint.
     */
    private void resetAutoIncrementCounters() {
        entityManager.flush();
        for (String table : RESET_TABLES) {
            entityManager.createNativeQuery("ALTER TABLE " + table + " AUTO_INCREMENT = 1").executeUpdate();
        }
    }
}