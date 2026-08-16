package org.example.healthcare.service;

import org.example.healthcare.dto.request.MedicalRecordRequest;
import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.dto.response.PatientHistoryResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AppointmentMapper;
import org.example.healthcare.mapper.MedicalRecordMapper;
import org.example.healthcare.mapper.PrescriptionMapper;
import org.example.healthcare.models.nosql.MedicalRecord;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.nosql.MedicalRecordRepository;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import org.example.healthcare.security.CallerGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final AppointmentMapper appointmentMapper;
    private final CallerGuard callerGuard;

    // ==================== CREATE (Doctor) ====================

    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {

        Patient patient = findPatientOrThrow(request.getPatientId());
        Doctor author = findDoctorOrThrow(callerGuard.currentDoctorId());

        MedicalRecord record = MedicalRecord.builder()
                .patientId(patient.getId())
                .patientName(patient.getName())
                .doctorId(author.getId())
                .doctorName(author.getName())
                .recordDate(LocalDate.now())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        try {
            return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to create medical record", ex);
        }
    }

    // ==================== GET ====================

    /** The patient's whole record: entries a doctor wrote, plus their appointments and prescriptions. */
    public PatientHistoryResponse getPatientHistory(Long patientId) {
        callerGuard.assertPatientOwns(patientId);
        Patient patient = findPatientOrThrow(patientId);

        try {
            return PatientHistoryResponse.builder()
                    .patientId(patient.getId())
                    .patientName(patient.getName())
                    .entries(medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId).stream()
                            .map(medicalRecordMapper::toResponse)
                            .collect(Collectors.toList()))
                    .appointments(appointmentRepository.findByPatientId(patientId).stream()
                            .sorted(Comparator.comparing(org.example.healthcare.models.sql.Appointment::getAppointmentDate)
                                    .thenComparing(org.example.healthcare.models.sql.Appointment::getStartTime)
                                    .reversed())
                            .map(appointmentMapper::toResponse)
                            .collect(Collectors.toList()))
                    .prescriptions(prescriptionRepository.findByPatientId(patientId).stream()
                            .map(prescriptionMapper::toResponse)
                            .collect(Collectors.toList()))
                    .build();
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to assemble the record for patient id: " + patientId, ex);
        }
    }

    public MedicalRecordResponse getMedicalRecordById(String id) {
        MedicalRecord record = findRecordOrThrow(id);
        callerGuard.assertParticipant(record.getPatientId(), record.getDoctorId());
        return medicalRecordMapper.toResponse(record);
    }

    // ==================== UPDATE (Doctor) ====================

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(String id, MedicalRecordRequest request) {
        MedicalRecord record = findRecordOrThrow(id);
        callerGuard.assertDoctorOwns(record.getDoctorId());

        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());

        try {
            return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update medical record with id: " + id, ex);
        }
    }

    // ==================== DELETE (Doctor) ====================

    @Transactional
    public void deleteMedicalRecord(String id) {
        MedicalRecord record = findRecordOrThrow(id);
        callerGuard.assertDoctorOwns(record.getDoctorId());

        try {
            medicalRecordRepository.delete(record);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete medical record with id: " + id, ex);
        }
    }

    // ==================== HELPERS ====================

    private MedicalRecord findRecordOrThrow(String id) {
        try {
            return medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch medical record with id: " + id, ex);
        }
    }

    private Patient findPatientOrThrow(Long id) {
        try {
            return patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch patient with id: " + id, ex);
        }
    }

    private Doctor findDoctorOrThrow(Long id) {
        try {
            return doctorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch doctor with id: " + id, ex);
        }
    }
}
