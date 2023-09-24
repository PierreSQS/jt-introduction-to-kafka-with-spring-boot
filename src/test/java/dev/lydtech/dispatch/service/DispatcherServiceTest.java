package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class DispatcherServiceTest {

    DispatcherService dispatcherService;

    @BeforeEach
    void setUp() {
        dispatcherService = new DispatcherService();
    }

    @Test
    void process() {
        OrderCreated orderCreatedTest =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());

        dispatcherService.process(orderCreatedTest);
    }
}