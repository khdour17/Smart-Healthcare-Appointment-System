package org.example.healthcare.controller;

import org.example.healthcare.dto.request.PatientRequest;
import org.example.healthcare.dto.response.MessageResponse;
import org.example.healthcare.dto.response.PatientResponse;
import org.example.healthcare.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ==================== GET (Admin only) ====================

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // ==================== UPDATE (Admin + Patient own) ====================

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    // ==================== DELETE (Admin only) ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(MessageResponse.builder().message("Patient deleted successfully").build());
    }
}