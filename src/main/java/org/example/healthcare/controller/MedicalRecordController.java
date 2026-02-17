package org.example.healthcare.controller;

import org.example.healthcare.dto.request.MedicalRecordRequest;
import org.example.healthcare.dto.response.MedicalRecordResponse;
import org.example.healthcare.dto.response.MessageResponse;
import org.example.healthcare.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // ==================== CREATE (Doctor) ====================

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.createMedicalRecord(request));
    }

    // ==================== GET ====================

    @GetMapping("/search")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecordById(@RequestParam String id) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientMedicalRecords(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getPatientMedicalRecords(patientId));
    }

    // ==================== UPDATE (Doctor) ====================

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable String id,
            @Valid @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.ok(medicalRecordService.updateMedicalRecord(id, request));
    }

    // ==================== DELETE (Doctor) ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteMedicalRecord(@PathVariable String id) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.ok(MessageResponse.builder().message("Medical record deleted successfully").build());
    }
}