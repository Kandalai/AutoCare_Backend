package com.autocare.controller;

import com.autocare.dto.MessageResponse;
import com.autocare.dto.VehicleRequest;
import com.autocare.model.User;
import com.autocare.model.Vehicle;
import com.autocare.repository.UserRepository;
import com.autocare.repository.VehicleRepository;
import com.autocare.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    UserRepository userRepository;

    // Helper method to get currently logged in user
    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> addVehicle(@Valid @RequestBody VehicleRequest request) {
        User user = getCurrentUser();
        Vehicle vehicle = new Vehicle(
                request.getMake(),
                request.getModel(),
                request.getYear(),
                request.getLicensePlate(),
                request.getMileage(),
                request.getColor(),
                request.getVin(),
                request.getImageUrl(),
                user
        );
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok(new MessageResponse("Vehicle added successfully!"));
    }

    // Mechanics and Admins can see all vehicles
    @GetMapping("/all")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getUserVehicles() {
        User user = getCurrentUser();
        List<Vehicle> vehicles = vehicleRepository.findByUserId(user.getId());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            // Ensure the user owns this vehicle
            if (vehicle.getUser().getId().equals(getCurrentUser().getId())) {
                return ResponseEntity.ok(vehicle);
            } else {
                return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized to view this vehicle."));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            if (vehicle.getUser().getId().equals(getCurrentUser().getId())) {
                vehicleRepository.delete(vehicle);
                return ResponseEntity.ok(new MessageResponse("Vehicle deleted successfully!"));
            } else {
                return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized to delete this vehicle."));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
