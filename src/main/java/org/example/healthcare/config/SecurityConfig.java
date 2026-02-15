package org.example.healthcare.config;

import org.example.healthcare.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    // ==================== ROLE CONSTANTS ====================
    private static final String ADMIN   = "ADMIN";
    private static final String DOCTOR  = "DOCTOR";
    private static final String PATIENT = "PATIENT";

    // ==================== PATH CONSTANTS ====================
    private static final String AUTH            = "/api/auth";
    private static final String ADMIN_API       = "/api/admin/**";
    private static final String DOCTORS_API     = "/api/doctors/**";
    private static final String PATIENTS_API    = "/api/patients/**";
    private static final String AVAILABILITY_API = "/api/availability/**";
    private static final String APPOINTMENTS_API = "/api/appointments/**";
    private static final String PRESCRIPTIONS_API = "/api/prescriptions/**";
    private static final String MEDICAL_RECORDS_API = "/api/medical-records/**";

    // Specific appointment actions (order matters — must match before broad patterns)
    private static final String APPOINTMENT_COMPLETE = "/api/appointments/*/complete";
    private static final String APPOINTMENT_CANCEL   = "/api/appointments/*/cancel";
    private static final String APPOINTMENT_BOOK     = "/api/appointments/patient/**";

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
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Return 401 for unauthenticated, 403 for unauthorized
                .exceptionHandling(ex -> ex
                        // No token or invalid token → 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\": \"Unauthorized: No valid token provided\"}");
                        })
                        // Valid token but wrong role → 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\": \"Forbidden: You don't have permission to access this resource\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC ──────────────────────────────────────────
                        .requestMatchers(AUTH + "/login").permitAll()

                        // ── ADMIN ONLY ─────────────────────────────────────
                        .requestMatchers(AUTH + "/register/**").hasAuthority(ADMIN)
                        .requestMatchers(ADMIN_API).hasAuthority(ADMIN)

                        // ── DOCTORS: view = all, modify = admin ────────────
                        .requestMatchers(HttpMethod.GET, DOCTORS_API).authenticated()
                        .requestMatchers(HttpMethod.PUT, DOCTORS_API).hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, DOCTORS_API).hasAuthority(ADMIN)

                        // ── PATIENTS: view = admin+doctor, update = admin+patient, delete = admin
                        .requestMatchers(HttpMethod.GET, PATIENTS_API).hasAnyAuthority(ADMIN, DOCTOR)
                        .requestMatchers(HttpMethod.PUT, PATIENTS_API).hasAnyAuthority(ADMIN, PATIENT)
                        .requestMatchers(HttpMethod.DELETE, PATIENTS_API).hasAuthority(ADMIN)

                        // ── AVAILABILITY: set = doctor, view = all ─────────
                        .requestMatchers(HttpMethod.POST, AVAILABILITY_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.DELETE, AVAILABILITY_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.GET, AVAILABILITY_API).authenticated()

                        // ── APPOINTMENTS (specific actions FIRST) ──────────
                        .requestMatchers(HttpMethod.PATCH, APPOINTMENT_COMPLETE).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.POST, APPOINTMENT_BOOK).hasAuthority(PATIENT)
                        .requestMatchers(HttpMethod.PATCH, APPOINTMENT_CANCEL).hasAuthority(PATIENT)
                        .requestMatchers(HttpMethod.GET, APPOINTMENTS_API).authenticated()

                        // ── PRESCRIPTIONS: write = doctor, read = doctor+patient
                        .requestMatchers(HttpMethod.POST, PRESCRIPTIONS_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.PUT, PRESCRIPTIONS_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.GET, PRESCRIPTIONS_API).hasAnyAuthority(DOCTOR, PATIENT)

                        // ── MEDICAL RECORDS: write = doctor, read = doctor+patient
                        .requestMatchers(HttpMethod.POST, MEDICAL_RECORDS_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.PUT, MEDICAL_RECORDS_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.DELETE, MEDICAL_RECORDS_API).hasAuthority(DOCTOR)
                        .requestMatchers(HttpMethod.GET, MEDICAL_RECORDS_API).hasAnyAuthority(DOCTOR, PATIENT)

                        // ── CATCH ALL ──────────────────────────────────────
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}