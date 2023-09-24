package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

class OrderCreatedConsumerTest {

    OrderCreatedConsumer orderCreatedConsumer;

    DispatcherService dispatcherServMock;

    @BeforeEach
    void setUp() {
        dispatcherServMock = mock(DispatcherService.class);
        orderCreatedConsumer = new OrderCreatedConsumer(dispatcherServMock);
    }

    @Test
    void listen_success() throws ExecutionException, InterruptedException {
        OrderCreated orderToSend = OrderCreated.builder()
                .uuid(UUID.randomUUID()).message(UUID.randomUUID().toString())
                .build();

        orderCreatedConsumer.listen(orderToSend);
        verify(dispatcherServMock).process(orderToSend);
    }
    @Test
    void listen_ServiceThrowsException() throws ExecutionException, InterruptedException {
        OrderCreated orderToSend = OrderCreated.builder()
                .uuid(UUID.randomUUID()).message(UUID.randomUUID().toString())
                .build();

        doThrow(new RuntimeException("Service failure")).when(dispatcherServMock).process(orderToSend);

        orderCreatedConsumer.listen(orderToSend);
        verify(dispatcherServMock).process(orderToSend);
    }
}