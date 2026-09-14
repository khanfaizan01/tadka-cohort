package com.tadka.service;

import com.tadka.domain.common.Result;
import com.tadka.domain.common.ResultT;
import com.tadka.domain.orders.IdempotencyKey;
import com.tadka.domain.orders.Order;
import com.tadka.domain.orders.OrderFactory;
import com.tadka.domain.orders.OrderStatus;
import com.tadka.domain.restaurants.Restaurant;
import com.tadka.domain.valueobjects.Address;
import com.tadka.repository.IIdempotencyStore;
import com.tadka.repository.IdempotencyKeyRepository;
import com.tadka.repository.OrderRepository;
import com.tadka.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final IIdempotencyStore idempotencyStore;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OrderFactory orderFactory;
    private final DomainEventDispatcher eventDispatcher;

    public OrderService(OrderRepository orderRepository,
                        RestaurantRepository restaurantRepository,
                        IIdempotencyStore idempotencyStore,
                        IdempotencyKeyRepository idempotencyKeyRepository,
                        OrderFactory orderFactory,
                        DomainEventDispatcher eventDispatcher) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.idempotencyStore = idempotencyStore;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.orderFactory = orderFactory;
        this.eventDispatcher = eventDispatcher;
    }

    @Transactional
    public ResultT<Order> placeOrder(UUID customerId, UUID restaurantId,
                                      List<OrderFactory.OrderItemRequest> items,
                                      Address deliveryAddress,
                                      String idempotencyKey) {
        // Idempotency check (ADR-011): if key exists, return the existing order
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            UUID existingOrderId = idempotencyStore.findOrderId(idempotencyKey);
            if (existingOrderId != null) {
                Order existing = orderRepository.findByIdWithItems(existingOrderId);
                if (existing != null) {
                    return ResultT.success(existing);
                }
            }
        }

        // Load restaurant with menu for server-side pricing
        Restaurant restaurant = restaurantRepository.findByIdWithMenu(restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        ResultT<Order> result = orderFactory.create(customerId, restaurant, items, deliveryAddress);
        if (result.isFailure()) {
            return ResultT.failure(result.getError());
        }

        Order order = result.getValue();
        orderRepository.save(order);

        // Record idempotency key in same transaction (same DbContext)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyKey ik = new IdempotencyKey();
            ik.setKey(idempotencyKey);
            ik.setOrderId(order.getId());
            ik.setCreatedAt(java.time.LocalDateTime.now());
            idempotencyKeyRepository.save(ik);
        }

        // Dispatch domain events AFTER commit (ADR-013)
        eventDispatcher.dispatch(order.getDomainEvents());
        order.clearDomainEvents();

        return ResultT.success(order);
    }

    @Transactional
    public ResultT<Order> transitionOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new EntityNotFoundException("Order not found");
        }

        Result result = order.transition(newStatus);
        if (result.isFailure()) {
            return ResultT.failure(result.getError());
        }

        orderRepository.save(order);

        eventDispatcher.dispatch(order.getDomainEvents());
        order.clearDomainEvents();
        return ResultT.success(order);
    }

    @Transactional
    public ResultT<Order> cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new EntityNotFoundException("Order not found");
        }

        Result result = order.cancel(reason);
        if (result.isFailure()) {
            return ResultT.failure(result.getError());
        }

        orderRepository.save(order);

        eventDispatcher.dispatch(order.getDomainEvents());
        order.clearDomainEvents();
        return ResultT.success(order);
    }

    @Transactional(readOnly = true)
    public Order getById(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> getByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getByCustomerIdPaginated(UUID customerId, int page, int size) {
        return orderRepository.findByCustomerIdPaginated(customerId, page, size);
    }
}
