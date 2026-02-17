package org.example.healthcare.controller;

import org.example.healthcare.dto.request.DoctorRequest;
import org.example.healthcare.dto.response.DoctorResponse;
import org.example.healthcare.dto.response.MessageResponse;
import org.example.healthcare.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // ==================== GET (Admin, Doctor, Patient) ====================

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/search")
    public ResponseEntity<DoctorResponse> getDoctorById(@RequestParam Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/specialty")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialty(@RequestParam String specialty) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialty));
    }

    // ==================== UPDATE (Admin only) ====================

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    // ==================== DELETE (Admin only) ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(MessageResponse.builder().message("Doctor deleted successfully").build());
    }
}