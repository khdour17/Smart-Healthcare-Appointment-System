package org.example.healthcare.service;

import org.example.healthcare.dto.request.MedicalRecordRequest;
import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.MedicalRecordMapper;
import org.example.healthcare.models.nosql.MedicalRecord;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
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

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.getPatientId()));

        MedicalRecord record = MedicalRecord.builder()
                .patientId(patient.getId())
                .patientName(patient.getName())
                .recordDate(request.getRecordDate())
                .title(request.getTitle())
                .description(request.getDescription())
                .prescriptionIds(request.getPrescriptionIds())
                .labReports(request.getLabReports())
                .build();

        MedicalRecord saved = medicalRecordRepository.save(record);
        return medicalRecordMapper.toResponse(saved);
    }

    // ==================== GET ====================

    public MedicalRecordResponse getMedicalRecordById(String id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
        return medicalRecordMapper.toResponse(record);
    }

    public List<MedicalRecordResponse> getPatientMedicalRecords(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId).stream()
                .map(medicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE (Doctor) ====================

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(String id, MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        record.setRecordDate(request.getRecordDate());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setPrescriptionIds(request.getPrescriptionIds());
        record.setLabReports(request.getLabReports());

        MedicalRecord updated = medicalRecordRepository.save(record);
        return medicalRecordMapper.toResponse(updated);
    }

    // ==================== DELETE (Doctor) ====================

    @Transactional
    public void deleteMedicalRecord(String id) {
        if (!medicalRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medical record not found with id: " + id);
        }
        medicalRecordRepository.deleteById(id);
    }
}