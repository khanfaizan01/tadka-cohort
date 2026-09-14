package com.tadka.controller.dto;

import jakarta.validation.constraints.*;

public class RedeemRequest {

    @NotNull
    private java.util.UUID customerId;

    public java.util.UUID getCustomerId() { return customerId; }
    public void setCustomerId(java.util.UUID customerId) { this.customerId = customerId; }
}
