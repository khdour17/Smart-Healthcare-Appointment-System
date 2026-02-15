package org.example.healthcare.service;

import org.example.healthcare.helpers.DoctorServiceTestHelper;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations (@Mock, etc.)
class DoctorServiceTest {

    @Mock // Creates a fake DoctorRepository (no real DB)
    private DoctorRepository doctorRepository;

    private DoctorServiceTestHelper helper;

    @BeforeEach
    void setUp() {
        // Real mapper (no logic to mock), fake repository
        helper = new DoctorServiceTestHelper(doctorRepository, new DoctorMapper());
    }

    @Test
    @DisplayName("Get all doctors returns list of doctor responses")
    void getAllDoctors() {
        helper.getAllDoctors_returnsList();
    }

    @Test
    @DisplayName("Get doctor by ID returns correct doctor")
    void getDoctorById_found() {
        helper.getDoctorById_found_returnsDoctor();
    }

    @Test
    @DisplayName("Get doctor by ID throws exception when not found")
    void getDoctorById_notFound() {
        helper.getDoctorById_notFound_throwsException();
    }

    @Test
    @DisplayName("Search doctors by specialty returns matching doctors")
    void getDoctorsBySpecialty() {
        helper.getDoctorsBySpecialty_returnsMatching();
    }

    @Test
    @DisplayName("Update doctor modifies and saves successfully")
    void updateDoctor_success() {
        helper.updateDoctor_success();
    }

    @Test
    @DisplayName("Update doctor throws exception when not found")
    void updateDoctor_notFound() {
        helper.updateDoctor_notFound_throwsException();
    }

    @Test
    @DisplayName("Delete doctor removes successfully")
    void deleteDoctor_success() {
        helper.deleteDoctor_success();
    }

    @Test
    @DisplayName("Delete doctor throws exception when not found")
    void deleteDoctor_notFound() {
        helper.deleteDoctor_notFound_throwsException();
    }
}