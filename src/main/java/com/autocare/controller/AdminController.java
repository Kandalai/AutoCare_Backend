package com.autocare.controller;

import com.autocare.model.User;
import com.autocare.model.Role;
import com.autocare.dto.CreateMechanicRequest;
import com.autocare.dto.MessageResponse;
import com.autocare.dto.UserSummaryResponse;
import com.autocare.repository.ServiceBookingRepository;
import com.autocare.repository.UserRepository;
import com.autocare.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ServiceBookingRepository bookingRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // Get system statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("totalBookings", bookingRepository.count());
        
        return ResponseEntity.ok(stats);
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserSummaryResponse::from).toList());
    }

    @PostMapping("/mechanics")
    public ResponseEntity<?> createMechanic(@Valid @RequestBody CreateMechanicRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Username is already taken."));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use."));
        }

        User mechanic = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()), Role.ROLE_MECHANIC);
        userRepository.save(mechanic);
        return ResponseEntity.ok(UserSummaryResponse.from(mechanic));
    }
}
