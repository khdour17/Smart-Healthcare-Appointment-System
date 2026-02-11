package org.example.healthcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Builder.Default
    private List<String> prescriptionIds = new ArrayList<>();

    @Builder.Default
    private List<String> labReports = new ArrayList<>();
}