package org.example.healthcare.service;

import org.example.healthcare.dto.request.PrescriptionRequest;
import org.example.healthcare.dto.response.PrescriptionResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.PrescriptionMapper;
import org.example.healthcare.models.nosql.Prescription;
import org.example.healthcare.models.sql.Appointment;
import org.example.healthcare.repository.nosql.PrescriptionRepository;
import org.example.healthcare.repository.sql.AppointmentRepository;
import lombok.RequiredArgsConstructor;
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
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

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

        Prescription saved = prescriptionRepository.save(prescription);
        return prescriptionMapper.toResponse(saved);
    }

    // ==================== GET ====================

    public PrescriptionResponse getPrescriptionById(String id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        return prescriptionMapper.toResponse(prescription);
    }

    public PrescriptionResponse getPrescriptionByAppointmentId(Long appointmentId) {
        Prescription prescription = prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found for appointment: " + appointmentId));
        return prescriptionMapper.toResponse(prescription);
    }

    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getDoctorPrescriptions(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE (Doctor) ====================

    @Transactional
    public PrescriptionResponse updatePrescription(String id, PrescriptionRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));

        prescription.setMedicines(request.getMedicines());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setInstructions(request.getInstructions());

        Prescription updated = prescriptionRepository.save(prescription);
        return prescriptionMapper.toResponse(updated);
    }
}