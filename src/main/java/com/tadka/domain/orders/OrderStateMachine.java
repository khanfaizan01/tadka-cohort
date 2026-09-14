package com.tadka.domain.orders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
        OrderStatus.CREATED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP),
        OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.PICKED_UP),
        OrderStatus.PICKED_UP, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED, Set.of(),
        OrderStatus.CANCELLED, Set.of(),
        OrderStatus.REFUNDED, Set.of()
    );

    public static boolean canTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = TRANSITIONS.get(current);
        return allowed != null && allowed.contains(next);
    }

    public static List<OrderStatus> getAllowedTransitions(OrderStatus current) {
        Set<OrderStatus> allowed = TRANSITIONS.get(current);
        return allowed != null ? Collections.unmodifiableList(new ArrayList<>(allowed)) : Collections.emptyList();
    }
}
