package org.example.healthcare.security;

import org.example.healthcare.exception.ForbiddenOperationException;
import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.Doctor;
import org.example.healthcare.models.sql.Patient;
import org.example.healthcare.models.sql.User;
import org.example.healthcare.repository.sql.DoctorRepository;
import org.example.healthcare.repository.sql.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Endpoint authorization only proves the caller holds a role, not that the record is theirs.
 * Without these checks a patient could read another patient's data, or a doctor could act on
 * an appointment belonging to a colleague, just by changing an id in the URL.
 * Admins pass every check.
 */
@Component
@RequiredArgsConstructor
public class CallerGuard {

    private static final String MESSAGE = "You can only access your own records";

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /** Patient-scoped data. Doctors and admins are clinical staff and may read it. */
    public void assertPatientOwns(Long patientId) {
        User user = currentUser();
        if (user.getRole() != Role.PATIENT) {
            return;
        }
        if (!callerPatientId(user).equals(patientId)) {
            throw new ForbiddenOperationException(MESSAGE);
        }
    }

    /** Doctor-scoped data. Patients have no business reading a doctor's whole book. */
    public void assertDoctorOwns(Long doctorId) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() != Role.DOCTOR || !callerDoctorId(user).equals(doctorId)) {
            throw new ForbiddenOperationException(MESSAGE);
        }
    }

    /** A record tied to one appointment: only its patient, its doctor, or an admin. */
    public void assertParticipant(Long patientId, Long doctorId) {
        User user = currentUser();
        switch (user.getRole()) {
            case ADMIN -> { }
            case PATIENT -> {
                if (!callerPatientId(user).equals(patientId)) {
                    throw new ForbiddenOperationException(MESSAGE);
                }
            }
            case DOCTOR -> {
                if (!callerDoctorId(user).equals(doctorId)) {
                    throw new ForbiddenOperationException(MESSAGE);
                }
            }
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenOperationException("Could not identify the current user");
        }
        return userDetails.getUser();
    }

    private Long callerPatientId(User user) {
        return patientRepository.findByUserId(user.getId())
                .map(Patient::getId)
                .orElseThrow(() -> new ForbiddenOperationException("No patient profile found for the current user"));
    }

    private Long callerDoctorId(User user) {
        return doctorRepository.findByUserId(user.getId())
                .map(Doctor::getId)
                .orElseThrow(() -> new ForbiddenOperationException("No doctor profile found for the current user"));
    }
}
