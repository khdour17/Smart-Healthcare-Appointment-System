package org.example.healthcare.dto.response;

import org.example.healthcare.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String username;
    private String email;
    private Role role;
}