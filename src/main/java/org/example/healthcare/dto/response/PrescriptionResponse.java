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
public class PrescriptionResponse {

    private String id;
    private Long appointmentId;
    private String patientName;
    private String doctorName;
    private LocalDate prescriptionDate;
    private List<String> medicines;
    private String diagnosis;
    private String instructions;
}