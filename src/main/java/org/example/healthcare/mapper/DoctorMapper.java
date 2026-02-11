package org.example.healthcare.mapper;

import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.models.sql.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .phone(doctor.getPhone())
                .isAvailable(doctor.getIsAvailable())
                .username(doctor.getUser().getUsername())
                .email(doctor.getUser().getEmail())
                .build();
    }
}