package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogPrescription;
import org.example.healthcare.dto.request.PrescriptionRequest;
import org.example.healthcare.dto.response.PrescriptionResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.PrescriptionMapper;
import org.example.healthcare.models.nosql.Prescription;
import org.example.healthcare.models.sql.Appointment;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper prescriptionMapper;

    // ==================== CREATE (Doctor) ====================

    @Transactional
    @LogPrescription(action = "CREATE")
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {

        Appointment appointment;
        try {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Appointment not found with id: " + request.getAppointmentId()));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch appointment with id: " + request.getAppointmentId(), ex);
        }

        Prescription prescription = Prescription.builder()
                .appointmentId(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .prescriptionDate(LocalDate.now())
                .medicines(request.getMedicines())
                .diagnosis(request.getDiagnosis())
                .instructions(request.getInstructions())
                .build();

        try {
            Prescription saved = prescriptionRepository.save(prescription);
            return prescriptionMapper.toResponse(saved);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to create prescription", ex);
        }
    }

    // ==================== GET ====================

    public PrescriptionResponse getPrescriptionById(String id) {
        try {
            Prescription prescription = prescriptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
            return prescriptionMapper.toResponse(prescription);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch prescription with id: " + id, ex);
        }
    }

    public PrescriptionResponse getPrescriptionByAppointmentId(Long appointmentId) {
        try {
            Prescription prescription = prescriptionRepository.findByAppointmentId(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Prescription not found for appointment: " + appointmentId));
            return prescriptionMapper.toResponse(prescription);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch prescription for appointment id: " + appointmentId, ex);
        }
    }

    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        try {
            return prescriptionRepository.findByPatientId(patientId).stream()
                    .map(prescriptionMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch prescriptions for patient id: " + patientId, ex);
        }
    }

    public List<PrescriptionResponse> getDoctorPrescriptions(Long doctorId) {
        try {
            return prescriptionRepository.findByDoctorId(doctorId).stream()
                    .map(prescriptionMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch prescriptions for doctor id: " + doctorId, ex);
        }
    }

    // ==================== UPDATE (Doctor) ====================

    @Transactional
    @LogPrescription(action = "UPDATE")
    public PrescriptionResponse updatePrescription(String id, PrescriptionRequest request) {
        Prescription prescription;
        try {
            prescription = prescriptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch prescription with id: " + id, ex);
        }

        prescription.setMedicines(request.getMedicines());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setInstructions(request.getInstructions());

        try {
            Prescription updated = prescriptionRepository.save(prescription);
            return prescriptionMapper.toResponse(updated);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update prescription with id: " + id, ex);
        }
    }
}