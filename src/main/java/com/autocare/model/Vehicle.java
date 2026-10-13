package com.autocare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(columnDefinition = "MEDIUMTEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    public Vehicle(String make, String model, Integer year, String licensePlate, Integer mileage, String color, String vin, String imageUrl, User user) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.mileage = mileage;
        this.color = color;
        this.vin = vin;
        this.imageUrl = imageUrl;
        this.user = user;
    }
}
