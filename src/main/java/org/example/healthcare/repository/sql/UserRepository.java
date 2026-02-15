package org.example.healthcare.repository.sql;

import org.example.healthcare.models.enums.Role;
import org.example.healthcare.models.sql.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    // Delete all users that are NOT the given role (keeps admin)
    void deleteAllByRoleNot(Role role);
}
