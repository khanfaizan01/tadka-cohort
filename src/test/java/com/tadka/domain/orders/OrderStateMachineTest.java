package com.tadka.domain.orders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {

    @Test
    void createdCanTransitionToConfirmedAndCancelled() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CREATED, OrderStatus.CONFIRMED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CREATED, OrderStatus.CANCELLED));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.CREATED, OrderStatus.PREPARING));
    }

    @Test
    void confirmedCanTransitionToPreparingAndCancelled() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.DELIVERED));
    }

    @Test
    void pickedUpCanTransitionToDeliveredOnly() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PICKED_UP, OrderStatus.DELIVERED));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.PICKED_UP, OrderStatus.CANCELLED));
    }

    @Test
    void deliveredAndCancelledAndRefundedAreTerminal() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.DELIVERED, OrderStatus.CONFIRMED));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.PREPARING));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.REFUNDED, OrderStatus.CONFIRMED));
    }

    @Test
    void getAllowedTransitionsReturnsCorrectList() {
        var allowed = OrderStateMachine.getAllowedTransitions(OrderStatus.CREATED);
        assertTrue(allowed.contains(OrderStatus.CONFIRMED));
        assertTrue(allowed.contains(OrderStatus.CANCELLED));
        assertEquals(2, allowed.size());
    }
}
