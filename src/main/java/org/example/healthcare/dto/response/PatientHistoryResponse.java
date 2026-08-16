package org.example.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A patient's medical record: everything on file for them, newest first.
 * Assembled from the entries doctors have written plus their appointments and prescriptions.
 */
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
