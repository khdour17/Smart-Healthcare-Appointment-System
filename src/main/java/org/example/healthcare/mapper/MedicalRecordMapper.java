package org.example.healthcare.mapper;

import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.models.nosql.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponse toResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientName(record.getPatientName())
                .recordDate(record.getRecordDate())
                .title(record.getTitle())
                .description(record.getDescription())
                .prescriptionIds(record.getPrescriptionIds())
                .labReports(record.getLabReports())
                .build();
    }
}