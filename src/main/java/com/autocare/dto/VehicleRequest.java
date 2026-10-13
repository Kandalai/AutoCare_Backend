package com.autocare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleRequest {
    @NotBlank
    private String make;

    @NotBlank
    private String model;

    @NotNull
    private Integer year;

    @NotBlank
    private String licensePlate;

    private Integer mileage;

    private String color;

    private String vin;

    private String imageUrl;
}
