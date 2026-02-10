package org.example.healthcare.controller;

import org.example.healthcare.dto.request.LoginRequest;
import org.example.healthcare.dto.request.RegisterAdminRequest;
import org.example.healthcare.dto.request.RegisterDoctorRequest;
import org.example.healthcare.dto.request.RegisterPatientRequest;
import org.example.healthcare.dto.response.JwtResponse;
import org.example.healthcare.dto.response.MessageResponse;
import org.example.healthcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<MessageResponse> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.builder().message("Admin registered successfully").build());
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<MessageResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest request) {
        authService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.builder().message("Doctor registered successfully").build());
    }

    @PostMapping("/register/patient")
    public ResponseEntity<MessageResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        authService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.builder().message("Patient registered successfully").build());
    }
}