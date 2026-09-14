package com.tadka.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemResponse {
    private UUID itemId;
    private UUID menuItemId;
    private String name;
    private int quantity;
    private MoneyResponse unitPrice;
    private String specialInstructions;

    public OrderItemResponse(UUID itemId, UUID menuItemId, String name, int quantity,
                             MoneyResponse unitPrice, String specialInstructions) {
        this.itemId = itemId; this.menuItemId = menuItemId; this.name = name;
        this.quantity = quantity; this.unitPrice = unitPrice; this.specialInstructions = specialInstructions;
    }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    public UUID getMenuItemId() { return menuItemId; }
    public void setMenuItemId(UUID menuItemId) { this.menuItemId = menuItemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public MoneyResponse getUnitPrice() { return unitPrice; }
    public void setUnitPrice(MoneyResponse unitPrice) { this.unitPrice = unitPrice; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
