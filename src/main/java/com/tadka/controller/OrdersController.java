package com.tadka.controller;

import com.tadka.controller.dto.*;
import com.tadka.domain.common.ResultT;
import com.tadka.domain.orders.Order;
import java.util.LinkedHashMap;
import java.util.Map;
import com.tadka.domain.orders.OrderFactory;
import com.tadka.domain.orders.OrderStatus;
import com.tadka.domain.valueobjects.Address;
import com.tadka.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Address address = new Address();
        address.setLine1(request.getDeliveryAddress().getLine1());
        address.setLine2(request.getDeliveryAddress().getLine2());
        address.setCity(request.getDeliveryAddress().getCity());
        address.setPincode(request.getDeliveryAddress().getPincode());
        address.setLatitude(request.getDeliveryAddress().getLatitude());
        address.setLongitude(request.getDeliveryAddress().getLongitude());

        List<OrderFactory.OrderItemRequest> items = request.getItems().stream()
            .map(r -> new OrderFactory.OrderItemRequest(r.getMenuItemId(), r.getQuantity(), r.getSpecialInstructions()))
            .toList();

        ResultT<Order> result = orderService.placeOrder(
            request.getCustomerId(), request.getRestaurantId(), items, address, idempotencyKey);

        if (result.isFailure()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", "ORDER_CANNOT_BE_PLACED");
            body.put("detail", result.getError());
            body.put("status", 422);
            body.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(422).body(body);
        }

        Order order = result.getValue();
        return ResponseEntity.created(URI.create("/api/v1/orders/" + order.getId()))
            .body(toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        Order order = orderService.getById(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping
    public ResponseEntity<OrderListResponse> listByCustomer(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getByCustomerIdPaginated(customerId, page, size);
        List<OrderResponse> responses = orders.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new OrderListResponse(responses, page, size, orders.size()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            ResultT<Order> result = orderService.transitionOrderStatus(id, newStatus);
            if (result.isFailure()) {
                return ResponseEntity.status(422).build();
            }
            return ResponseEntity.noContent().build();
        } catch (OptimisticLockingFailureException e) {
            throw e; // handled by GlobalExceptionHandler → 409
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelOrderRequest request) {
        try {
            ResultT<Order> result = orderService.cancelOrder(id, request.getReason());
            if (result.isFailure()) {
                return ResponseEntity.status(422).build();
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage()); // handled by GlobalExceptionHandler → 422
        }
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
            .map(i -> new OrderItemResponse(
                i.getId(), i.getMenuItemId(), i.getName(), i.getQuantity(),
                new MoneyResponse(i.getUnitPrice().getAmount(), i.getUnitPrice().getCurrency()),
                i.getSpecialInstructions()))
            .toList();

        Address a = o.getDeliveryAddress();
        AddressResponse address = new AddressResponse(
            a.getLine1(), a.getLine2(), a.getCity(), a.getPincode(), a.getLatitude(), a.getLongitude());

        return new OrderResponse(
            o.getId(), o.getCustomerId(), o.getRestaurantId(), o.getStatus().name(),
            items,
            new MoneyResponse(o.getTotalAmount().getAmount(), o.getTotalAmount().getCurrency()),
            address,
            o.getCreatedAt(), o.getConfirmedAt(), o.getDeliveredAt(),
            o.getCancelledAt(), o.getCancellationReason());
    }
}
