package org.example.healthcare.service;

import org.example.healthcare.helpers.PatientServiceTestHelper;
import org.example.healthcare.mapper.PatientMapper;
import org.example.healthcare.repository.sql.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientServiceTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new PatientServiceTestHelper(patientRepository, new PatientMapper());
    }

    @Test
    @DisplayName("Get all patients returns list")
    void getAllPatients() {
        helper.getAllPatients_returnsList();
    }

    @Test
    @DisplayName("Get patient by ID returns correct patient")
    void getPatientById_found() {
        helper.getPatientById_found_returnsPatient();
    }

    @Test
    @DisplayName("Get patient by ID throws when not found")
    void getPatientById_notFound() {
        helper.getPatientById_notFound_throwsException();
    }

    @Test
    @DisplayName("Update patient saves successfully")
    void updatePatient_success() {
        helper.updatePatient_success();
    }

    @Test
    @DisplayName("Update patient throws when not found")
    void updatePatient_notFound() {
        helper.updatePatient_notFound_throwsException();
    }

    @Test
    @DisplayName("Delete patient removes successfully")
    void deletePatient_success() {
        helper.deletePatient_success();
    }

    @Test
    @DisplayName("Delete patient throws when not found")
    void deletePatient_notFound() {
        helper.deletePatient_notFound_throwsException();
    }
}