package com.tadka.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateOrderStatusRequest {

    @NotBlank
    @Pattern(regexp = "(?i)CREATED|CONFIRMED|PREPARING|READY_FOR_PICKUP|PICKED_UP|DELIVERED|CANCELLED|REFUNDED",
             message = "Invalid order status")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
