package org.example.healthcare.service;

import org.example.healthcare.dto.request.PatientRequest;
import org.example.healthcare.dto.response.PatientResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.PatientMapper;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import org.example.healthcare.security.CallerGuard;
import org.example.healthcare.repository.sql.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CallerGuard callerGuard;
    private final PatientMapper patientMapper;

    // ==================== GET ====================

    public List<PatientResponse> getAllPatients() {
        try {
            return patientRepository.findAll().stream()
                    .map(patientMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch all patients", ex);
        }
    }

    public PatientResponse getPatientById(Long id) {
        Patient patient = findPatientOrThrow(id);
        return patientMapper.toResponse(patient);
    }

    // ==================== UPDATE ====================

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = findPatientOrThrow(id);
        callerGuard.assertPatientOwns(id);

        patient.setName(request.getName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        try {
            Patient updated = patientRepository.save(patient);
            return patientMapper.toResponse(updated);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update patient with id: " + id, ex);
        }
    }

    // ==================== DELETE ====================

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = findPatientOrThrow(id);
        User user = patient.getUser();
        try {
            appointmentRepository.deleteByPatientIdIn(List.of(id));
            patientRepository.delete(patient);
            // Flush so the patient row is gone before its users row is removed (FK: patients.user_id -> users.id)
            patientRepository.flush();
            if (user != null) {
                userRepository.delete(user);
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete patient with id: " + id, ex);
        }
    }
    
    // ==================== DELETE (bulk) ====================

    @Transactional
    public void deletePatients(List<Long> ids) {
        try {
            List<Patient> patientsToDelete = patientRepository.findAllById(ids);
            if (patientsToDelete.size() != ids.size()) {
                throw new ResourceNotFoundException("One or more patients not found for the given ids");
            }
            List<User> usersToDelete = patientsToDelete.stream()
                    .map(Patient::getUser)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            appointmentRepository.deleteByPatientIdIn(ids);
            patientRepository.deleteAll(patientsToDelete);
            // Flush so the patient rows are gone before their users rows are removed (FK: patients.user_id -> users.id)
            patientRepository.flush();
            userRepository.deleteAll(usersToDelete);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete patients with ids: " + ids, ex);
        }
    }

    // ==================== HELPER ====================

    private Patient findPatientOrThrow(Long id) {
        try {
            return patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch patient with id: " + id, ex);
        }
    }
}