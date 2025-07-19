package dev.lydtech.dispatch.util;

import java.util.UUID;

import dev.lydtech.dispatch.event.DispatchPreparing;
import dev.lydtech.dispatch.event.OrderCreated;

public class TestEventData {

    public static OrderCreated buildOrderCreatedEvent(UUID orderId, String item) {
        return OrderCreated.builder()
                .orderId(orderId)
                .item(item)
                .build();
    }

    // Not in use at the moment, but kept for reference
    public static DispatchPreparing buildDispatchPreparingEvent(UUID orderId) {
        return DispatchPreparing.builder()
                .orderId(orderId)
                .build();
    }
}
