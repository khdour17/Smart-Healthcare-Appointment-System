package org.example.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {

    private String id;
    private String patientName;
    private LocalDate recordDate;
    private String title;
    private String description;
    private List<String> prescriptionIds;
    private List<String> labReports;
}