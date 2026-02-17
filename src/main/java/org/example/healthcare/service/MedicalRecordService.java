package org.example.healthcare.service;

import org.example.healthcare.dto.request.MedicalRecordRequest;
import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.MedicalRecordMapper;
import org.example.healthcare.models.nosql.MedicalRecord;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    // ==================== CREATE (Doctor) ====================

    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {

        Patient patient;
        try {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Patient not found with id: " + request.getPatientId()));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch patient with id: " + request.getPatientId(), ex);
        }

        MedicalRecord record = MedicalRecord.builder()
                .patientId(patient.getId())
                .patientName(patient.getName())
                .recordDate(request.getRecordDate())
                .title(request.getTitle())
                .description(request.getDescription())
                .prescriptionIds(request.getPrescriptionIds())
                .labReports(request.getLabReports())
                .build();

        try {
            MedicalRecord saved = medicalRecordRepository.save(record);
            return medicalRecordMapper.toResponse(saved);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to create medical record", ex);
        }
    }

    // ==================== GET ====================

    public MedicalRecordResponse getMedicalRecordById(String id) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
            return medicalRecordMapper.toResponse(record);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch medical record with id: " + id, ex);
        }
    }

    public List<MedicalRecordResponse> getPatientMedicalRecords(Long patientId) {
        try {
            return medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId).stream()
                    .map(medicalRecordMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch medical records for patient id: " + patientId, ex);
        }
    }

    // ==================== UPDATE (Doctor) ====================

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(String id, MedicalRecordRequest request) {
        MedicalRecord record;
        try {
            record = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch medical record with id: " + id, ex);
        }

        record.setRecordDate(request.getRecordDate());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setPrescriptionIds(request.getPrescriptionIds());
        record.setLabReports(request.getLabReports());

        try {
            MedicalRecord updated = medicalRecordRepository.save(record);
            return medicalRecordMapper.toResponse(updated);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update medical record with id: " + id, ex);
        }
    }

    // ==================== DELETE (Doctor) ====================

    @Transactional
    public void deleteMedicalRecord(String id) {
        try {
            if (!medicalRecordRepository.existsById(id)) {
                throw new ResourceNotFoundException("Medical record not found with id: " + id);
            }
            medicalRecordRepository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete medical record with id: " + id, ex);
        }
    }
}