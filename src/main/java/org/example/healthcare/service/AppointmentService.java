package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogAppointment;
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

    // ==================== BOOK ====================

    @Transactional
    @LogAppointment(action = "BOOK")
    public AppointmentResponse bookAppointment(Long patientId, AppointmentRequest request) {

        // 1. Validate patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        // 2. Validate doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        // 3. Get availability for this day
        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(request.getDoctorId(), request.getAppointmentDate().getDayOfWeek())
                .orElseThrow(() -> new DoubleBookingException(
                        "Doctor not available on " + request.getAppointmentDate().getDayOfWeek()));

        // 4. Calculate end time from slot duration
        LocalTime endTime = request.getStartTime().plusMinutes(availability.getSlotDurationMinutes());

        // 5. Validate within working hours
        if (request.getStartTime().isBefore(availability.getStartTime()) ||
                endTime.isAfter(availability.getEndTime())) {
            throw new DoubleBookingException("Time outside doctor's working hours (" +
                    availability.getStartTime() + " - " + availability.getEndTime() + ")");
        }

        // 6. Check double booking
        Long overlappingCount = appointmentRepository.countOverlappingAppointments(
                request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), endTime);

        if (overlappingCount > 0) {
            throw new DoubleBookingException("Time slot already booked for this doctor");
        }

        // 7. Create appointment
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    // ==================== AVAILABLE SLOTS ====================

    @Transactional(readOnly = true)
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
        appointmentRepository.save(appointment);
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

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    // ==================== GET ====================

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        return appointmentMapper.toResponse(findAppointmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
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