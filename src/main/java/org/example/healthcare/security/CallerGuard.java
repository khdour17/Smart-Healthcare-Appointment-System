package org.example.healthcare.security;

import org.example.healthcare.exception.ForbiddenOperationException;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Endpoint authorization only proves the caller holds a role, not that the record is theirs.
 * Without this a patient could read or change another patient's data by guessing an id.
 */
@Component
@RequiredArgsConstructor
public class CallerGuard {

    private final PatientRepository patientRepository;

    public void assertPatientOwns(Long patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenOperationException("Could not identify the current user");
        }

        User user = userDetails.getUser();
        if (user.getRole() != Role.PATIENT) {
            return;
        }

        Long callerPatientId = patientRepository.findByUserId(user.getId())
                .map(Patient::getId)
                .orElseThrow(() -> new ForbiddenOperationException("No patient profile found for the current user"));

        if (!callerPatientId.equals(patientId)) {
            throw new ForbiddenOperationException("You can only access your own records");
        }
    }
}
