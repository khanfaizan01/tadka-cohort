package com.tadka.domain.orders;

import com.tadka.domain.common.Result;
import com.tadka.domain.common.ResultT;
import com.tadka.domain.orders.Events.OrderPlacedEvent;
import com.tadka.domain.restaurants.MenuItem;
import com.tadka.domain.restaurants.Restaurant;
import com.tadka.domain.valueobjects.Address;
import com.tadka.domain.valueobjects.Money;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class OrderFactory {

    public ResultT<Order> create(UUID customerId, Restaurant restaurant,
                                  List<OrderItemRequest> requestedItems,
                                  Address deliveryAddress) {
        java.util.Set<UUID> requestedIds = requestedItems.stream()
            .map(OrderItemRequest::getMenuItemId).collect(Collectors.toSet());

        List<MenuItem> menu = restaurant.getMenu();
        if (menu == null) menu = List.of();
        java.util.Map<UUID, MenuItem> menuLookup = menu.stream()
            .filter(m -> requestedIds.contains(m.getId()))
            .collect(Collectors.toMap(MenuItem::getId, m -> m));

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest req : requestedItems) {
            MenuItem menuItem = menuLookup.get(req.getMenuItemId());
            if (menuItem == null) {
                return ResultT.failure(
                    "Menu item '" + req.getMenuItemId() + "' not found in restaurant '" + restaurant.getName() + "'.");
            }
            if (!menuItem.getIsAvailable()) {
                return ResultT.failure("'" + menuItem.getName() + "' is currently unavailable.");
            }
            orderItems.add(new OrderItem(
                UUID.randomUUID(),
                null,
                menuItem.getId(),
                menuItem.getName(),
                req.getQuantity(),
                new Money(menuItem.getPrice().getAmount(), menuItem.getPrice().getCurrency()),
                req.getSpecialInstructions()
            ));
        }

        BigDecimal total = orderItems.stream()
            .map(i -> i.getUnitPrice().getAmount().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(customerId);
        order.setRestaurantId(restaurant.getId());
        order.setStatus(OrderStatus.CREATED);
        order.setItems(orderItems);
        // Bidirectional link: set order reference on each item so @JoinColumn(order_id) is populated
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setTotalAmount(new Money(total, "INR"));
        order.setDeliveryAddress(deliveryAddress);
        order.setCreatedAt(java.time.LocalDateTime.now());

        order.raise(new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getRestaurantId()));

        return ResultT.success(order);
    }

    // DTO-like input record for the factory
    public static class OrderItemRequest {
        private UUID menuItemId;
        private int quantity;
        private String specialInstructions;

        public OrderItemRequest() {}
        public OrderItemRequest(UUID menuItemId, int quantity, String specialInstructions) {
            this.menuItemId = menuItemId; this.quantity = quantity; this.specialInstructions = specialInstructions;
        }
        public UUID getMenuItemId() { return menuItemId; }
        public void setMenuItemId(UUID menuItemId) { this.menuItemId = menuItemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    }
}
