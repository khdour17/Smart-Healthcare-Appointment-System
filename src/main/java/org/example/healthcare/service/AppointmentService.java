package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogAppointment;
import org.example.healthcare.dto.request.AppointmentRequest;
import org.example.healthcare.dto.response.AppointmentResponse;
import org.example.healthcare.dto.response.AvailableSlotResponse;
import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.exception.DatabaseOperationException;
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
import org.springframework.dao.DataAccessException;
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

    // ==================== BOOK ====================

    @Transactional
    @LogAppointment(action = "BOOK")
    public AppointmentResponse bookAppointment(Long patientId, AppointmentRequest request) {

        Patient patient = findPatientOrThrow(patientId);
        Doctor doctor = findDoctorOrThrow(request.getDoctorId());

        DoctorAvailability availability = findAvailabilityOrThrow(
                request.getDoctorId(), request.getAppointmentDate());

        LocalTime endTime = request.getStartTime().plusMinutes(availability.getSlotDurationMinutes());

        if (request.getStartTime().isBefore(availability.getStartTime()) ||
                endTime.isAfter(availability.getEndTime())) {
            throw new DoubleBookingException("Time outside doctor's working hours (" +
                    availability.getStartTime() + " - " + availability.getEndTime() + ")");
        }

        try {
            Long overlappingCount = appointmentRepository.countOverlappingAppointments(
                    request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), endTime);

            if (overlappingCount > 0) {
                throw new DoubleBookingException("Time slot already booked for this doctor");
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to check appointment availability", ex);
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        try {
            return appointmentMapper.toResponse(appointmentRepository.save(appointment));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to book appointment", ex);
        }
    }

    // ==================== AVAILABLE SLOTS ====================

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {

        Doctor doctor = findDoctorOrThrow(doctorId);
        DoctorAvailability availability = findAvailabilityOrThrow(doctorId, date);

        List<Appointment> bookedAppointments;
        try {
            bookedAppointments = appointmentRepository.findBookedAppointments(doctorId, date);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch booked appointments for doctor id: " + doctorId, ex);
        }

        List<AvailableSlotResponse> availableSlots = new ArrayList<>();
        LocalTime current = availability.getStartTime();
        int duration = availability.getSlotDurationMinutes();

        while (!current.plusMinutes(duration).isAfter(availability.getEndTime())) {

            LocalTime slotEnd = current.plusMinutes(duration);
            LocalTime slotStart = current;

            boolean isBooked = bookedAppointments.stream()
                    .anyMatch(apt -> slotStart.isBefore(apt.getEndTime()) && slotEnd.isAfter(apt.getStartTime()));

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

    // ==================== CANCEL (Patient) ====================

    @Transactional
    @LogAppointment(action = "CANCEL")
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only scheduled appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        try {
            appointmentRepository.save(appointment);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to cancel appointment with id: " + appointmentId, ex);
        }
    }

    // ==================== COMPLETE (Doctor) ====================

    @Transactional
    @LogAppointment(action = "COMPLETE")
    public AppointmentResponse completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only scheduled appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setNotes(notes);

        try {
            return appointmentMapper.toResponse(appointmentRepository.save(appointment));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to complete appointment with id: " + appointmentId, ex);
        }
    }

    // ==================== GET ====================

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        return appointmentMapper.toResponse(findAppointmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        try {
            return appointmentRepository.findByPatientId(patientId).stream()
                    .map(appointmentMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch appointments for patient id: " + patientId, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        try {
            return appointmentRepository.findByDoctorId(doctorId).stream()
                    .map(appointmentMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch appointments for doctor id: " + doctorId, ex);
        }
    }

    // ==================== HELPERS ====================

    private Appointment findAppointmentOrThrow(Long id) {
        try {
            return appointmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch appointment with id: " + id, ex);
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

    private DoctorAvailability findAvailabilityOrThrow(Long doctorId, LocalDate date) {
        try {
            return availabilityRepository
                    .findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())
                    .orElseThrow(() -> new DoubleBookingException(
                            "Doctor not available on " + date.getDayOfWeek()));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch availability for doctor id: " + doctorId, ex);
        }
    }
}