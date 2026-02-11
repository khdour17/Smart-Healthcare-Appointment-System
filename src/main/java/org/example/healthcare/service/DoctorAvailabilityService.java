package org.example.healthcare.service;

import org.example.healthcare.dto.request.DoctorAvailabilityRequest;
import org.example.healthcare.dto.response.DoctorAvailabilityResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.DoctorAvailabilityMapper;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.DoctorAvailability;
import org.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityMapper availabilityMapper;

    // ==================== SET AVAILABILITY (Doctor sets own) ====================

    @Transactional
    public DoctorAvailabilityResponse setAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        // Update if exists for this day, otherwise create new
        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek())
                .orElse(new DoctorAvailability());

        availability.setDoctor(doctor);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());

        DoctorAvailability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponse(saved);
    }

    // ==================== GET ====================

    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }

        return availabilityRepository.findByDoctorId(doctorId).stream()
                .map(availabilityMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== DELETE ====================

    @Transactional
    public void deleteAvailability(Long id) {
        if (!availabilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Availability not found with id: " + id);
        }
        availabilityRepository.deleteById(id);
    }
}