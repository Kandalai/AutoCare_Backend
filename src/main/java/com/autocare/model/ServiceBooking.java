package com.autocare.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "service_bookings")
@Data
@NoArgsConstructor
public class ServiceBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"user"}) // Prevent infinite recursion but fetch vehicle details
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mechanic_id")
    @JsonIgnoreProperties({"password"})
    private User mechanic;

    @NotBlank
    private String serviceType; // e.g., Oil Change, Brake Inspection

    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(length = 500)
    private String notes;

    public ServiceBooking(Vehicle vehicle, User customer, String serviceType, LocalDate bookingDate) {
        this.vehicle = vehicle;
        this.customer = customer;
        this.serviceType = serviceType;
        this.bookingDate = bookingDate;
        this.status = BookingStatus.PENDING;
    }
}
