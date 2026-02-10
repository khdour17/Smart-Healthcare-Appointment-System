package org.example.healthcare.config;

import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.Admin;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.AdminRepository;
import org.example.healthcare.repository.sql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (userRepository.findByRole(Role.ADMIN).isEmpty()) {

                User adminUser = User.builder()
                        .username("admin")
                        .email("admin@healthcare.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();
                User savedUser = userRepository.save(adminUser);

                Admin admin = Admin.builder()
                        .user(savedUser)
                        .name("System Admin")
                        .phone("000-0000")
                        .department("IT")
                        .build();
                adminRepository.save(admin);

                log.info("========================================");
                log.info("Default admin created:");
                log.info("Username: admin");
                log.info("Password: admin123");
                log.info("========================================");
            } else {
                log.info("Admin already exists, skipping seed.");
            }
        } catch (Exception e) {
            log.error("Failed to seed default admin: {}", e.getMessage(), e);
        }
    }
}