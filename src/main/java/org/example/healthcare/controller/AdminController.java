package org.example.healthcare.controller;

import org.example.healthcare.dto.response.AdminResponse;
import org.example.healthcare.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/search")
    public ResponseEntity<AdminResponse> getAdminById(@RequestParam Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDatabase() {
        adminService.resetDatabase();
        return ResponseEntity.ok(Map.of("message", "Database reset successfully. Admin account preserved."));
    }
}