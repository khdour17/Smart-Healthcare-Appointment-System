package org.example.healthcare.service;

import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.repository.sql.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    // ==================== GET ====================

    // Cached: all doctors list
    @Cacheable(value = "allDoctors")
    public List<DoctorResponse> getAllDoctors() {
        log.info("[CACHE MISS] Fetching all doctors from database");
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Cached: single doctor by ID
    @Cacheable(value = "doctorById", key = "#id")
    public DoctorResponse getDoctorById(Long id) {
        log.info("[CACHE MISS] Fetching doctor from database | ID: {}", id);
        return doctorMapper.toResponse(findDoctorOrThrow(id));
    }

    // Cached: doctors by specialty
    @Cacheable(value = "doctorsBySpecialty", key = "#specialty.toLowerCase()")
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        log.info("[CACHE MISS] Fetching doctors by specialty from database | Specialty: {}", specialty);
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
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = findDoctorOrThrow(id);
        doctor.setName(request.getName());
        doctor.setSpecialty(request.getSpecialty());
        log.info("[CACHE EVICT] Doctor updated, cache cleared | ID: {}", id);
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    // ==================== DELETE (evicts cache) ====================

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allDoctors", allEntries = true),
            @CacheEvict(value = "doctorById", key = "#id"),
            @CacheEvict(value = "doctorsBySpecialty", allEntries = true)
    })
    public void deleteDoctor(Long id) {
        log.info("[CACHE EVICT] Doctor deleted, cache cleared | ID: {}", id);
        doctorRepository.delete(findDoctorOrThrow(id));
    }

    // ==================== HELPER ====================

    private Doctor findDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
}