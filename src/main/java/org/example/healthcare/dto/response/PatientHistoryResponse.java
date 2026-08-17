package org.example.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHistoryResponse {

    private Long patientId;
    private String patientName;
    private List<MedicalRecordResponse> entries;
    private List<AppointmentResponse> appointments;
    private List<PrescriptionResponse> prescriptions;
}
