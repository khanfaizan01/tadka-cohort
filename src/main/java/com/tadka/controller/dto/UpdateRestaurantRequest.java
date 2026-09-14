package com.tadka.controller.dto;

public class UpdateRestaurantRequest {

    private String name;
    private Integer avgPrepTimeMinutes;
    private Boolean isActive;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAvgPrepTimeMinutes() { return avgPrepTimeMinutes; }
    public void setAvgPrepTimeMinutes(Integer avgPrepTimeMinutes) { this.avgPrepTimeMinutes = avgPrepTimeMinutes; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
