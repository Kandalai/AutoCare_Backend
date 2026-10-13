package com.autocare.controller;

import com.autocare.dto.BookingRequest;
import com.autocare.dto.MessageResponse;
import com.autocare.dto.UserSummaryResponse;
import com.autocare.model.*;
import com.autocare.repository.ServiceBookingRepository;
import com.autocare.repository.UserRepository;
import com.autocare.repository.VehicleRepository;
import com.autocare.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    ServiceBookingRepository bookingRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Get all available mechanics for customers to choose from
    @GetMapping("/mechanics")
    public ResponseEntity<List<UserSummaryResponse>> getAvailableMechanics() {
        List<UserSummaryResponse> mechanics = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_MECHANIC)
                .map(UserSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(mechanics);
    }

    // Customer creates a booking with Optional Mechanic Selection or Automated Assignment
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
        User customer = getCurrentUser();
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Error: Vehicle not found."));

        if (!vehicle.getUser().getId().equals(customer.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You can only book services for your own vehicles."));
        }

        ServiceBooking booking = new ServiceBooking(vehicle, customer, request.getServiceType(), request.getBookingDate());

        // --- MECHANIC SELECTION LOGIC ---
        if (request.getMechanicId() != null) {
            // Customer explicitly picked a mechanic
            User selectedMechanic = userRepository.findById(request.getMechanicId())
                    .orElseThrow(() -> new RuntimeException("Error: Mechanic not found."));
            if (selectedMechanic.getRole() != Role.ROLE_MECHANIC) {
                return ResponseEntity.badRequest().body(new MessageResponse("Selected account is not a mechanic."));
            }
            booking.setMechanic(selectedMechanic);
            booking.setStatus(BookingStatus.ACCEPTED);
        } else {
            // Automated Admin Logic: Auto-assign a mechanic
            List<User> mechanics = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_MECHANIC)
                    .toList();
            
            if (!mechanics.isEmpty()) {
                int randomIndex = new java.util.Random().nextInt(mechanics.size());
                User assignedMechanic = mechanics.get(randomIndex);
                booking.setMechanic(assignedMechanic);
                booking.setStatus(BookingStatus.ACCEPTED);
            } else {
                booking.setStatus(BookingStatus.PENDING);
            }
        }
        // --------------------------------

        bookingRepository.save(booking);

        if (booking.getMechanic() != null) {
            String msg = request.getMechanicId() != null ? "You successfully chose Mechanic " : "System auto-assigned Mechanic ";
            return ResponseEntity.ok(new MessageResponse("Service booked! " + msg + booking.getMechanic().getUsername() + " for your vehicle."));
        }

        return ResponseEntity.ok(new MessageResponse("Service booked successfully! Waiting for a mechanic to join the platform."));
    }

    // Get bookings for current user (Customer or Mechanic)
    @GetMapping("/my-bookings")
    public ResponseEntity<List<ServiceBooking>> getMyBookings() {
        User user = getCurrentUser();
        List<ServiceBooking> bookings;
        if (user.getRole() == Role.ROLE_MECHANIC) {
            bookings = bookingRepository.findByMechanicId(user.getId());
        } else {
            bookings = bookingRepository.findByCustomerId(user.getId());
        }
        return ResponseEntity.ok(bookings);
    }

    // Admin or mechanic can see all bookings.
    @GetMapping("/all")
    public ResponseEntity<List<ServiceBooking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAllByOrderByBookingDateDesc());
    }

    // Mechanic accepts a booking
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptBooking(@PathVariable Long id) {
        ServiceBooking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        User mechanic = getCurrentUser();
        
        if (mechanic.getRole() != Role.ROLE_MECHANIC && mechanic.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body(new MessageResponse("Unauthorized"));
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            return ResponseEntity.badRequest().body(new MessageResponse("Only pending bookings can be accepted."));
        }

        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(new MessageResponse("Booking accepted successfully!"));
    }

    // Mechanic updates status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        ServiceBooking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                (currentUser.getRole() != Role.ROLE_MECHANIC || booking.getMechanic() == null ||
                        !booking.getMechanic().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(403).body(new MessageResponse("You can only update jobs assigned to you."));
        }
        
        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            if (newStatus == BookingStatus.PENDING || newStatus == BookingStatus.REJECTED) {
                return ResponseEntity.badRequest().body(new MessageResponse("This status cannot be selected here."));
            }
            if (currentUser.getRole() != Role.ROLE_ADMIN &&
                    ((booking.getStatus() == BookingStatus.ACCEPTED && newStatus != BookingStatus.IN_PROGRESS) ||
                            (booking.getStatus() == BookingStatus.IN_PROGRESS && newStatus != BookingStatus.COMPLETED) ||
                            booking.getStatus() == BookingStatus.COMPLETED)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid job status transition."));
            }
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            return ResponseEntity.ok(new MessageResponse("Status updated to " + newStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid status"));
        }
    }
}
