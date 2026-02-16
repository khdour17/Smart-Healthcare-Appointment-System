package org.example.healthcare.service;

import org.example.healthcare.aspect.annotation.LogDoctor;
import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.repository.sql.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "doctorById", key = "#id")
    @LogDoctor(action = "GET_BY_ID", cacheAction = "MISS")
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(findDoctorOrThrow(id));
    }

    @Cacheable(value = "doctorsBySpecialty", key = "#specialty.toLowerCase()")
    @LogDoctor(action = "GET_BY_SPECIALTY", cacheAction = "MISS")
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty).stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
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
        return doctorMapper.toResponse(doctorRepository.save(doctor));
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
        doctorRepository.delete(findDoctorOrThrow(id));
    }

    // ==================== HELPER ====================

    private Doctor findDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
}