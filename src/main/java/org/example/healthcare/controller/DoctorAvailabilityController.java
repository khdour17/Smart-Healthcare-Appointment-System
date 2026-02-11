package org.example.healthcare.controller;

import org.example.healthcare.dto.request.DoctorAvailabilityRequest;
import org.example.healthcare.dto.response.DoctorAvailabilityResponse;
import org.example.healthcare.dto.response.MessageResponse;
import org.example.healthcare.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    @PostMapping("/doctor/{doctorId}")
    public ResponseEntity<DoctorAvailabilityResponse> setAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.setAvailability(doctorId, request));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getDoctorAvailability(@PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getDoctorAvailability(doctorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(MessageResponse.builder().message("Availability deleted successfully").build());
    }
}