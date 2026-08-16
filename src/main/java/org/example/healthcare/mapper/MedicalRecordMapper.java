package org.example.healthcare.mapper;

import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.models.nosql.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponse toResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .patientName(record.getPatientName())
                .doctorName(record.getDoctorName())
                .recordDate(record.getRecordDate())
                .title(record.getTitle())
                .description(record.getDescription())
                .build();
    }
}
