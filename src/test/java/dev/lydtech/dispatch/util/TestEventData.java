package dev.lydtech.dispatch.util;

import dev.lydtech.dispatch.message.OrderCreated;

import java.util.UUID;

public class TestEventData {
    public static OrderCreated buildOrderCreatedEvent(UUID uuid, String item) {
        return OrderCreated.builder()
                .uuid(uuid)
                .message(item)
                .build();
    }
}
