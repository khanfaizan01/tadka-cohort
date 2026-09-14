package com.tadka.controller.dto;

public class AddressResponse {
    private String line1;
    private String line2;
    private String city;
    private String pincode;
    private Double latitude;
    private Double longitude;

    public AddressResponse(String line1, String line2, String city, String pincode,
                           Double latitude, Double longitude) {
        this.line1 = line1; this.line2 = line2; this.city = city; this.pincode = pincode;
        this.latitude = latitude; this.longitude = longitude;
    }

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
