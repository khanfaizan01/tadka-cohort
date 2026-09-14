package com.tadka.controller.dto;

import jakarta.validation.constraints.*;

public class CreateRestaurantRequest {

    @NotBlank
    private String name;

    @NotNull
    private AddressRequest address;

    private Integer avgPrepTimeMinutes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AddressRequest getAddress() { return address; }
    public void setAddress(AddressRequest address) { this.address = address; }
    public Integer getAvgPrepTimeMinutes() { return avgPrepTimeMinutes; }
    public void setAvgPrepTimeMinutes(Integer avgPrepTimeMinutes) { this.avgPrepTimeMinutes = avgPrepTimeMinutes; }
}
