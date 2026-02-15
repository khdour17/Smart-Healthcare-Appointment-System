package org.example.healthcare.service;

import org.example.healthcare.helpers.AppointmentServiceTestHelper;
import org.example.healthcare.mapper.AppointmentMapper;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorAvailabilityRepository availabilityRepository;

    private AppointmentServiceTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new AppointmentServiceTestHelper(
                appointmentRepository, patientRepository, doctorRepository,
                availabilityRepository, new AppointmentMapper());
    }

    // ── Group related tests with @Nested for readability ──

    @Nested
    @DisplayName("Booking Appointments")
    class BookingTests {

        @Test
        @DisplayName("Successfully book appointment in available slot")
        void success() {
            helper.bookAppointment_success();
        }

        @Test
        @DisplayName("Reject double booking for same doctor and time")
        void doubleBooking() {
            helper.bookAppointment_doubleBooking_throwsException();
        }

        @Test
        @DisplayName("Reject booking when doctor not found")
        void doctorNotFound() {
            helper.bookAppointment_doctorNotFound_throwsException();
        }

        @Test
        @DisplayName("Reject booking when patient not found")
        void patientNotFound() {
            helper.bookAppointment_patientNotFound_throwsException();
        }

        @Test
        @DisplayName("Reject booking when doctor not available on requested day")
        void doctorNotAvailableOnDay() {
            helper.bookAppointment_doctorNotAvailableOnDay_throwsException();
        }

        @Test
        @DisplayName("Reject booking outside doctor's working hours")
        void outsideWorkingHours() {
            helper.bookAppointment_outsideWorkingHours_throwsException();
        }
    }

    @Nested
    @DisplayName("Cancelling Appointments")
    class CancelTests {

        @Test
        @DisplayName("Successfully cancel scheduled appointment")
        void success() {
            helper.cancelAppointment_success();
        }

        @Test
        @DisplayName("Reject cancellation of completed appointment")
        void alreadyCompleted() {
            helper.cancelAppointment_alreadyCompleted_throwsException();
        }
    }

    @Nested
    @DisplayName("Completing Appointments")
    class CompleteTests {

        @Test
        @DisplayName("Successfully complete appointment with notes")
        void success() {
            helper.completeAppointment_success();
        }

        @Test
        @DisplayName("Reject completion of cancelled appointment")
        void alreadyCancelled() {
            helper.completeAppointment_alreadyCancelled_throwsException();
        }
    }
}