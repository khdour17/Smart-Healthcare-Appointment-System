package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogDoctor;
import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.exception.DatabaseOperationException;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.repository.sql.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    // ==================== GET ====================

    @Cacheable(value = "allDoctors")
    @LogDoctor(action = "GET_ALL", cacheAction = "MISS")
    public List<DoctorResponse> getAllDoctors() {
        try {
            return doctorRepository.findAll().stream()
                    .map(doctorMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch all doctors", ex);
        }
    }

    @Cacheable(value = "doctorById", key = "#id")
    @LogDoctor(action = "GET_BY_ID", cacheAction = "MISS")
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(findDoctorOrThrow(id));
    }

    @Cacheable(value = "doctorsBySpecialty", key = "#specialty.toLowerCase()")
    @LogDoctor(action = "GET_BY_SPECIALTY", cacheAction = "MISS")
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        try {
            return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty).stream()
                    .map(doctorMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch doctors by specialty: " + specialty, ex);
        }
    }

    // ==================== UPDATE (evicts cache) ====================

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allDoctors", allEntries = true),
            @CacheEvict(value = "doctorById", key = "#id"),
            @CacheEvict(value = "doctorsBySpecialty", allEntries = true)
    })
    @LogDoctor(action = "UPDATE", cacheAction = "EVICT")
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

    // ==================== DELETE (evicts cache) ====================

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allDoctors", allEntries = true),
            @CacheEvict(value = "doctorById", key = "#id"),
            @CacheEvict(value = "doctorsBySpecialty", allEntries = true)
    })
    @LogDoctor(action = "DELETE", cacheAction = "EVICT")
    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorOrThrow(id);
        try {
            doctorRepository.delete(doctor);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete doctor with id: " + id, ex);
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