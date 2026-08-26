package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogDoctor;
import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.AppointmentRepository;
import org.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    // ==================== GET ====================

    @LogDoctor(action = "GET_ALL")
    public List<DoctorResponse> getAllDoctors() {
        try {
            return doctorRepository.findAll().stream()
                    .map(doctorMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch all doctors", ex);
        }
    }

    @LogDoctor(action = "GET_BY_ID")
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(findDoctorOrThrow(id));
    }

    @LogDoctor(action = "GET_BY_SPECIALTY")
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        try {
            return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty).stream()
                    .map(doctorMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch doctors by specialty: " + specialty, ex);
        }
    }

    // ==================== UPDATE ====================

    @Transactional
    @LogDoctor(action = "UPDATE")
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = findDoctorOrThrow(id);
        doctor.setName(request.getName());
        doctor.setSpecialty(request.getSpecialty());
        try {
            return doctorMapper.toResponse(doctorRepository.save(doctor));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update doctor with id: " + id, ex);
        }
    }

    // ==================== DELETE ====================

    @Transactional
    @LogDoctor(action = "DELETE")
    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorOrThrow(id);
        User user = doctor.getUser();
        try {
            appointmentRepository.deleteByDoctorIdIn(List.of(id));
            doctorAvailabilityRepository.deleteByDoctorIdIn(List.of(id));
            doctorRepository.delete(doctor);
            // Flush so the doctor row is gone before its users row is removed (FK: doctors.user_id -> users.id)
            doctorRepository.flush();
            if (user != null) {
                userRepository.delete(user);
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete doctor with id: " + id, ex);
        }
    }

    // ==================== DELETE (bulk) ====================

    @Transactional
    @LogDoctor(action = "DELETE_BULK")
    public void deleteDoctors(List<Long> ids) {
        try {
            List<Doctor> doctorsToDelete = doctorRepository.findAllById(ids);
            if (doctorsToDelete.size() != ids.size()) {
                throw new ResourceNotFoundException("One or more doctors not found for the given ids");
            }
            List<User> usersToDelete = doctorsToDelete.stream()
                    .map(Doctor::getUser)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            appointmentRepository.deleteByDoctorIdIn(ids);
            doctorAvailabilityRepository.deleteByDoctorIdIn(ids);
            doctorRepository.deleteAll(doctorsToDelete);
            // Flush so the doctor rows are gone before their users rows are removed (FK: doctors.user_id -> users.id)
            doctorRepository.flush();
            userRepository.deleteAll(usersToDelete);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete doctors with ids: " + ids, ex);
        }
    }

    // ==================== HELPER ====================

    private Doctor findDoctorOrThrow(Long id) {
        try {
            return doctorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch doctor with id: " + id, ex);
        }
    }
}