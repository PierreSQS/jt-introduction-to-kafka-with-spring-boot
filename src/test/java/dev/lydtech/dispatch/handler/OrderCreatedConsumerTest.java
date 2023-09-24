package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderCreatedConsumerTest {

    OrderCreatedConsumer orderCreatedConsumer;

    DispatcherService dispatcherServMock;

    @BeforeEach
    void setUp() {
        dispatcherServMock = mock(DispatcherService.class);
        orderCreatedConsumer = new OrderCreatedConsumer(dispatcherServMock);
    }

    @Test
    void listen() {
        OrderCreated orderToSend = OrderCreated.builder()
                .uuid(UUID.randomUUID()).message(UUID.randomUUID().toString())
                .build();

        orderCreatedConsumer.listen(orderToSend);
        verify(dispatcherServMock).process(orderToSend);
    }
}