package org.example.healthcare.service;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.exception.ResourceNotFoundException;
import org.example.healthcare.mapper.AdminMapper;
import org.example.healthcare.repository.sql.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AdminResponse getAdminById(Long id) {
        return adminRepository.findById(id)
                .map(adminMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }
}