package org.example.healthcare.helpers;

import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.service.DoctorService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Helper for DoctorService tests.
 * Each method is ONE test scenario — setup, execute, assert.
 */
public class DoctorServiceTestHelper {

    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;

    public DoctorServiceTestHelper(DoctorRepository doctorRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.doctorService = new DoctorService(doctorRepository, doctorMapper);
    }

    // ── GET ALL ───────────────────────────────────────────────

    /** Verify that getAllDoctors returns mapped responses for all doctors in DB */
    public void getAllDoctors_returnsList() {
        // Arrange: two doctors in the "database"
        Doctor doctor1 = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
        Doctor doctor2 = TestDataHelper.createDoctor(2L, "Dr. Jones", "Neurology");
        when(doctorRepository.findAll()).thenReturn(List.of(doctor1, doctor2));

        // Act
        List<DoctorResponse> result = doctorService.getAllDoctors();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        assertEquals("Dr. Jones", result.get(1).getName());

        // Verify repository was called exactly once
        verify(doctorRepository, times(1)).findAll();
    }

    // ── GET BY ID ─────────────────────────────────────────────

    /** Verify getDoctorById returns correct doctor when found */
    public void getDoctorById_found_returnsDoctor() {
        Doctor doctor = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse result = doctorService.getDoctorById(1L);

        assertEquals("Dr. Smith", result.getName());
        assertEquals("Cardiology", result.getSpecialty());
    }

    /** Verify getDoctorById throws exception when doctor not found */
    public void getDoctorById_notFound_throwsException() {
        // Arrange: repository returns empty
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: should throw ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> doctorService.getDoctorById(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
    }

    // ── GET BY SPECIALTY ──────────────────────────────────────

    /** Verify searching by specialty returns matching doctors */
    public void getDoctorsBySpecialty_returnsMatching() {
        Doctor doctor = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
        when(doctorRepository.findBySpecialtyContainingIgnoreCase("cardio"))
                .thenReturn(List.of(doctor));

        List<DoctorResponse> result = doctorService.getDoctorsBySpecialty("cardio");

        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getSpecialty());
    }

    // ── UPDATE ────────────────────────────────────────────────

    /** Verify updateDoctor modifies and saves the doctor */
    public void updateDoctor_success() {
        // Arrange: existing doctor
        Doctor doctor = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // Act: update name and specialty
        DoctorRequest request = DoctorRequest.builder()
                .name("Dr. Smith Updated")
                .specialty("Heart Surgery")
                .build();
        DoctorResponse result = doctorService.updateDoctor(1L, request);

        // Assert: save was called
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    /** Verify updateDoctor throws when doctor not found */
    public void updateDoctor_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                doctorService.updateDoctor(99L, DoctorRequest.builder()
                        .name("X").specialty("Y").build()));
    }

    // ── DELETE ────────────────────────────────────────────────

    /** Verify deleteDoctor removes the doctor */
    public void deleteDoctor_success() {
        Doctor doctor = TestDataHelper.createDoctor(1L, "Dr. Smith", "Cardiology");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // Act
        doctorService.deleteDoctor(1L);

        // Assert: delete was called with the correct doctor
        verify(doctorRepository, times(1)).delete(doctor);
    }

    /** Verify deleteDoctor throws when doctor not found */
    public void deleteDoctor_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                doctorService.deleteDoctor(99L));
    }
}