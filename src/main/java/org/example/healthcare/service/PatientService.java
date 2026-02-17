package org.example.healthcare.service;

import org.example.healthcare.dto.request.PatientRequest;
import org.example.healthcare.dto.response.PatientResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.PatientMapper;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
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
        try {
            patientRepository.delete(patient);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete patient with id: " + id, ex);
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