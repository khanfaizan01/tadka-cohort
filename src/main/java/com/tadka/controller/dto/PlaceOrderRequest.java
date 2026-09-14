package com.tadka.controller.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public class PlaceOrderRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID restaurantId;

    @Size(min = 1)
    private List<OrderItemRequest> items;

    @NotNull
    private AddressRequest deliveryAddress;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    public void setRestaurantId(UUID restaurantId) { this.restaurantId = restaurantId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public AddressRequest getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(AddressRequest deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
