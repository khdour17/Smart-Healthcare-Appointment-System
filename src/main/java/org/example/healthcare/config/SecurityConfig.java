package org.example.healthcare.config;

import org.example.healthcare.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/api/auth/register/**",
            "/api/admin/**"
    };

    private static final String[] DOCTOR_ENDPOINTS = {
            "/api/prescriptions/**",
            "/api/availability/**"
    };

    private static final String[] PATIENT_ENDPOINTS = {
            "/api/appointments/**",
            "/api/medical-records/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Admin only
                        .requestMatchers(ADMIN_ENDPOINTS).hasAuthority("ADMIN")

                        // Doctors: everyone can view, only admin can create/update/delete
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**").hasAnyAuthority("ADMIN", "PATIENT", "DOCTOR")
                        .requestMatchers("/api/doctors/**").hasAuthority("ADMIN")

                        // Patients: admin full control, patient can update own
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasAnyAuthority("ADMIN", "PATIENT")
                        .requestMatchers("/api/patients/**").hasAuthority("ADMIN")

                        // Doctor actions
                        .requestMatchers(DOCTOR_ENDPOINTS).hasAuthority("DOCTOR")

                        // Appointment completion is doctor action
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/*/complete").hasAuthority("DOCTOR")

                        // Patient actions
                        .requestMatchers(PATIENT_ENDPOINTS).hasAuthority("PATIENT")

                        // Everything else
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}