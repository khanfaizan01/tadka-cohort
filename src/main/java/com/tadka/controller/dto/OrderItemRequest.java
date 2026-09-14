package com.tadka.controller.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class OrderItemRequest {

    @NotNull
    private UUID menuItemId;

    @Min(1)
    private int quantity;

    private String specialInstructions;

    public UUID getMenuItemId() { return menuItemId; }
    public void setMenuItemId(UUID menuItemId) { this.menuItemId = menuItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
