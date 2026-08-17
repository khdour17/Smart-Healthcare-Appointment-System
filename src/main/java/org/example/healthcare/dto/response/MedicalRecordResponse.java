package org.example.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {

    private String id;
    private Long patientId;
    private String patientName;
    private String doctorName;
    private LocalDate recordDate;
    private String title;
    private String description;
}
