package com.tadka.controller.dto;

import jakarta.validation.constraints.*;

public class AddressRequest {

    @NotBlank
    private String line1;

    private String line2;

    @NotBlank
    private String city;

    @NotBlank
    private String pincode;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
