package org.example.healthcare.service;

import org.example.healthcare.dto.request.AppointmentRequest;
import org.example.healthcare.dto.response.AppointmentResponse;
import org.example.healthcare.dto.response.AvailableSlotResponse;
import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.exception.DoubleBookingException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AppointmentMapper;
import org.example.healthcare.models.sql.Appointment;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.DoctorAvailability;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentMapper appointmentMapper;

    // ==================== BOOK (Patient) ====================

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, AppointmentRequest request) {

        // 1. Validate patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        // 2. Validate doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        // 3. Check doctor works on this day
        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(request.getDoctorId(), request.getAppointmentDate().getDayOfWeek())
                .orElseThrow(() -> new DoubleBookingException(
                        "Doctor not available on " + request.getAppointmentDate().getDayOfWeek()));

        // 4. Check time is within working hours
        if (request.getStartTime().isBefore(availability.getStartTime()) ||
                request.getEndTime().isAfter(availability.getEndTime())) {
            throw new DoubleBookingException("Time outside doctor's working hours (" +
                    availability.getStartTime() + " - " + availability.getEndTime() + ")");
        }

        // 5. Check for double booking (HQL overlap query)
        Long overlappingCount = appointmentRepository.countOverlappingAppointments(
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (overlappingCount > 0) {
            throw new DoubleBookingException("Time slot already booked for this doctor");
        }

        // 6. Create appointment
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    // ==================== AVAILABLE SLOTS (Patient views) ====================

    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not available on " + date.getDayOfWeek()));

        // Get already booked appointments
        List<Appointment> bookedAppointments = appointmentRepository.findBookedAppointments(doctorId, date);

        // Generate all possible slots and filter out booked ones
        List<AvailableSlotResponse> availableSlots = new ArrayList<>();
        LocalTime current = availability.getStartTime();

        while (current.plusMinutes(availability.getSlotDurationMinutes()).isBefore(availability.getEndTime())
                || current.plusMinutes(availability.getSlotDurationMinutes()).equals(availability.getEndTime())) {

            LocalTime slotEnd = current.plusMinutes(availability.getSlotDurationMinutes());

            LocalTime slotStart = current;
            boolean isBooked = bookedAppointments.stream()
                    .anyMatch(apt -> isOverlapping(slotStart, slotEnd, apt.getStartTime(), apt.getEndTime()));

            if (!isBooked) {
                availableSlots.add(AvailableSlotResponse.builder()
                        .doctorId(doctorId)
                        .doctorName(doctor.getName())
                        .slotDate(date)
                        .startTime(current)
                        .endTime(slotEnd)
                        .build());
            }

            current = slotEnd;
        }

        return availableSlots;
    }

    private boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    // ==================== CANCEL (Patient) ====================

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    // ==================== COMPLETE (Doctor) ====================

    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot complete a cancelled appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Appointment is already completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setNotes(notes);

        Appointment updated = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(updated);
    }

    // ==================== GET ====================

    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        return appointmentMapper.toResponse(appointment);
    }

    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER ====================

    private Appointment findAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }
}