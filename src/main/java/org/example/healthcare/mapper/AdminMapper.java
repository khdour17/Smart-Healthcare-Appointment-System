package org.example.healthcare.mapper;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.models.sql.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AdminResponse toResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .department(admin.getDepartment())
                .username(admin.getUser().getUsername())
                .email(admin.getUser().getEmail())
                .build();
    }
}