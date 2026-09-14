package com.tadka.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private String status;
    private List<OrderItemResponse> items;
    private MoneyResponse totalAmount;
    private AddressResponse deliveryAddress;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    public OrderResponse(UUID orderId, UUID customerId, UUID restaurantId, String status,
                         List<OrderItemResponse> items, MoneyResponse totalAmount,
                         AddressResponse deliveryAddress, LocalDateTime createdAt,
                         LocalDateTime confirmedAt, LocalDateTime deliveredAt,
                         LocalDateTime cancelledAt, String cancellationReason) {
        this.orderId = orderId; this.customerId = customerId; this.restaurantId = restaurantId;
        this.status = status; this.items = items; this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress; this.createdAt = createdAt;
        this.confirmedAt = confirmedAt; this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt; this.cancellationReason = cancellationReason;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    public void setRestaurantId(UUID restaurantId) { this.restaurantId = restaurantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public MoneyResponse getTotalAmount() { return totalAmount; }
    public void setTotalAmount(MoneyResponse totalAmount) { this.totalAmount = totalAmount; }
    public AddressResponse getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(AddressResponse deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
}
