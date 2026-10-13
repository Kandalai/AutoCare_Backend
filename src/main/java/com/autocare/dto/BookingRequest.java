package com.autocare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull
    private Long vehicleId;

    @NotBlank
    private String serviceType;

    @NotNull
    private LocalDate bookingDate;

    // Optional: Let customer pick a specific mechanic
    private Long mechanicId;
}
