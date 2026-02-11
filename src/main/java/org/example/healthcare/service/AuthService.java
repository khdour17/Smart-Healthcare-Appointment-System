package org.example.healthcare.service;

import org.example.healthcare.dto.request.LoginRequest;
import org.example.healthcare.dto.request.RegisterAdminRequest;
import org.example.healthcare.dto.request.RegisterDoctorRequest;
import org.example.healthcare.dto.request.RegisterPatientRequest;
import org.example.healthcare.dto.response.JwtResponse;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.exception.DuplicateResourceException;
import org.example.healthcare.models.sql.Admin;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.AdminRepository;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import org.example.healthcare.repository.sql.UserRepository;
import org.example.healthcare.security.CustomUserDetails;
import org.example.healthcare.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ==================== LOGIN ====================

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ==================== REGISTER ADMIN ====================

    @Transactional
    public void registerAdmin(RegisterAdminRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.ADMIN);

        adminRepository.save(Admin.builder()
                .user(savedUser)
                .name(request.getName())
                .department(request.getDepartment())
                .build());
    }

    // ==================== REGISTER DOCTOR ====================

    @Transactional
    public void registerDoctor(RegisterDoctorRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.DOCTOR);

        doctorRepository.save(Doctor.builder()
                .user(savedUser)
                .name(request.getName())
                .specialty(request.getSpecialty())
                .build());
    }

    // ==================== REGISTER PATIENT ====================

    @Transactional
    public void registerPatient(RegisterPatientRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.PATIENT);

        patientRepository.save(Patient.builder()
                .user(savedUser)
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
    }

    // ==================== HELPERS ====================

    private void validateNewUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private User createUser(String username, String email, String password, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build());
    }
}