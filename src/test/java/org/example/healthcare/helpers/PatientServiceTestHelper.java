package org.example.healthcare.helpers;

import org.example.healthcare.dto.request.PatientRequest;
import org.example.healthcare.dto.response.PatientResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.PatientMapper;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.repository.sql.PatientRepository;
import org.example.healthcare.service.PatientService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Helper for PatientService tests.
 */
public class PatientServiceTestHelper {

    private final PatientService patientService;
    private final PatientRepository patientRepository;

    public PatientServiceTestHelper(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientService = new PatientService(patientRepository, patientMapper);
    }

    // ── GET ALL ───────────────────────────────────────────────

    /** Verify getAllPatients returns all patients */
    public void getAllPatients_returnsList() {
        Patient p1 = TestDataHelper.createPatient(1L, "John Doe");
        Patient p2 = TestDataHelper.createPatient(2L, "Jane Doe");
        when(patientRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PatientResponse> result = patientService.getAllPatients();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Doe", result.get(1).getName());
        verify(patientRepository, times(1)).findAll();
    }

    // ── GET BY ID ─────────────────────────────────────────────

    /** Verify getPatientById returns correct patient */
    public void getPatientById_found_returnsPatient() {
        Patient patient = TestDataHelper.createPatient(1L, "John Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientResponse result = patientService.getPatientById(1L);

        assertEquals("John Doe", result.getName());
        assertEquals(LocalDate.of(1990, 5, 15), result.getDateOfBirth());
    }

    /** Verify getPatientById throws when not found */
    public void getPatientById_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientById(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
    }

    // ── UPDATE ────────────────────────────────────────────────

    /** Verify updatePatient modifies and saves */
    public void updatePatient_success() {
        Patient patient = TestDataHelper.createPatient(1L, "John Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientRequest request = PatientRequest.builder()
                .name("John Updated")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phone("555-9999")
                .address("456 New St")
                .build();

        PatientResponse result = patientService.updatePatient(1L, request);

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    /** Verify updatePatient throws when not found */
    public void updatePatient_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                patientService.updatePatient(99L, PatientRequest.builder()
                        .name("X")
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .build()));
    }

    // ── DELETE ────────────────────────────────────────────────

    /** Verify deletePatient removes successfully */
    public void deletePatient_success() {
        Patient patient = TestDataHelper.createPatient(1L, "John Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.deletePatient(1L);

        verify(patientRepository, times(1)).delete(patient);
    }

    /** Verify deletePatient throws when not found */
    public void deletePatient_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                patientService.deletePatient(99L));
    }
}