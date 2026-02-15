package org.example.healthcare.helpers;

import org.example.healthcare.dto.request.AppointmentRequest;
import org.example.healthcare.dto.response.AppointmentResponse;
import org.example.healthcare.models.enums.AppointmentStatus;
import org.example.healthcare.exception.DoubleBookingException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AppointmentMapper;
import org.example.healthcare.models.sql.*;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import org.example.healthcare.service.AppointmentService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Helper for AppointmentService tests.
 * Focuses on booking logic and double-booking prevention.
 */
public class AppointmentServiceTestHelper {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    // Shared test data
    private final Doctor doctor = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
    private final Patient patient = TestDataHelper.createPatient(1L, "John Doe");

    // Wednesday 2026-02-11 — doctor works 09:00-17:00, 30min slots
    private final DoctorAvailability availability =
            TestDataHelper.createAvailability(1L, doctor, DayOfWeek.WEDNESDAY);
    private final LocalDate appointmentDate = LocalDate.of(2026, 2, 11); // Wednesday

    public AppointmentServiceTestHelper(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository availabilityRepository,
            AppointmentMapper appointmentMapper) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentService = new AppointmentService(
                appointmentRepository, patientRepository, doctorRepository,
                availabilityRepository, appointmentMapper);
    }

    // ── BOOKING SUCCESS ───────────────────────────────────────

    /** Happy path: patient books available slot successfully */
    public void bookAppointment_success() {
        // Arrange
        AppointmentRequest request = createRequest(LocalTime.of(9, 0));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.WEDNESDAY))
                .thenReturn(Optional.of(availability));
        when(appointmentRepository.countOverlappingAppointments(
                eq(1L), eq(appointmentDate), any(), any())).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(TestDataHelper.createAppointment(
                        1L, patient, doctor, appointmentDate,
                        LocalTime.of(9, 0), LocalTime.of(9, 30)));

        // Act
        AppointmentResponse result = appointmentService.bookAppointment(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("Dr. Smith", result.getDoctorName());
        assertEquals("John Doe", result.getPatientName());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(9, 30), result.getEndTime()); // auto-calculated!
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    // ── DOUBLE BOOKING ────────────────────────────────────────

    /** Core requirement: system prevents double booking same doctor, same time */
    public void bookAppointment_doubleBooking_throwsException() {
        AppointmentRequest request = createRequest(LocalTime.of(10, 0));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.WEDNESDAY))
                .thenReturn(Optional.of(availability));

        // Simulate existing booking at same time
        when(appointmentRepository.countOverlappingAppointments(
                eq(1L), eq(appointmentDate), any(), any())).thenReturn(1L);

        // Act & Assert
        DoubleBookingException exception = assertThrows(
                DoubleBookingException.class,
                () -> appointmentService.bookAppointment(1L, request)
        );

        assertEquals("Time slot already booked for this doctor", exception.getMessage());

        // Verify save was NEVER called — booking was rejected
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    // ── DOCTOR NOT FOUND ──────────────────────────────────────

    /** Booking fails when doctor doesn't exist */
    public void bookAppointment_doctorNotFound_throwsException() {
        AppointmentRequest request = createRequest(LocalTime.of(9, 0));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(1L, request));

        verify(appointmentRepository, never()).save(any());
    }

    // ── PATIENT NOT FOUND ─────────────────────────────────────

    /** Booking fails when patient doesn't exist */
    public void bookAppointment_patientNotFound_throwsException() {
        AppointmentRequest request = createRequest(LocalTime.of(9, 0));

        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(99L, request));

        verify(appointmentRepository, never()).save(any());
    }

    // ── DOCTOR NOT AVAILABLE ON DAY ───────────────────────────

    /** Booking fails when doctor doesn't work on requested day */
    public void bookAppointment_doctorNotAvailableOnDay_throwsException() {
        // Request for Monday, but doctor only works Wednesday
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(1L)
                .appointmentDate(LocalDate.of(2026, 2, 9)) // Monday
                .startTime(LocalTime.of(9, 0))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());

        assertThrows(DoubleBookingException.class,
                () -> appointmentService.bookAppointment(1L, request));

        verify(appointmentRepository, never()).save(any());
    }

    // ── OUTSIDE WORKING HOURS ─────────────────────────────────

    /** Booking fails when time is outside doctor's working hours */
    public void bookAppointment_outsideWorkingHours_throwsException() {
        // 07:00 is before doctor's 09:00 start
        AppointmentRequest request = createRequest(LocalTime.of(7, 0));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.WEDNESDAY))
                .thenReturn(Optional.of(availability));

        DoubleBookingException exception = assertThrows(
                DoubleBookingException.class,
                () -> appointmentService.bookAppointment(1L, request)
        );

        assertTrue(exception.getMessage().contains("working hours"));
        verify(appointmentRepository, never()).save(any());
    }

    // ── CANCEL SUCCESS ────────────────────────────────────────

    /** Scheduled appointment can be cancelled */
    public void cancelAppointment_success() {
        Appointment appointment = TestDataHelper.createAppointment(
                1L, patient, doctor, appointmentDate,
                LocalTime.of(9, 0), LocalTime.of(9, 30));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        appointmentService.cancelAppointment(1L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    // ── CANCEL ALREADY COMPLETED ──────────────────────────────

    /** Cannot cancel a completed appointment */
    public void cancelAppointment_alreadyCompleted_throwsException() {
        Appointment appointment = TestDataHelper.createAppointment(
                1L, patient, doctor, appointmentDate,
                LocalTime.of(9, 0), LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelAppointment(1L));

        verify(appointmentRepository, never()).save(any());
    }

    // ── COMPLETE SUCCESS ──────────────────────────────────────

    /** Doctor completes appointment with notes */
    public void completeAppointment_success() {
        Appointment appointment = TestDataHelper.createAppointment(
                1L, patient, doctor, appointmentDate,
                LocalTime.of(9, 0), LocalTime.of(9, 30));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse result = appointmentService.completeAppointment(1L, "Patient examined");

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        assertEquals("Patient examined", appointment.getNotes());
    }

    // ── COMPLETE ALREADY CANCELLED ────────────────────────────

    /** Cannot complete a cancelled appointment */
    public void completeAppointment_alreadyCancelled_throwsException() {
        Appointment appointment = TestDataHelper.createAppointment(
                1L, patient, doctor, appointmentDate,
                LocalTime.of(9, 0), LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.completeAppointment(1L, "notes"));

        verify(appointmentRepository, never()).save(any());
    }

    // ── HELPER ────────────────────────────────────────────────

    private AppointmentRequest createRequest(LocalTime startTime) {
        return AppointmentRequest.builder()
                .doctorId(1L)
                .appointmentDate(appointmentDate)
                .startTime(startTime)
                .reason("Checkup")
                .build();
    }
}