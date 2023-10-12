package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

class OrderCreatedConsumerTest {

    OrderCreatedConsumer orderCreatedConsumer;

    DispatchService dispatcherServMock;

    String messageKey;

    OrderCreated orderToSend;

    @BeforeEach
    void setUp() {
        dispatcherServMock = mock(DispatchService.class);
        orderCreatedConsumer = new OrderCreatedConsumer(dispatcherServMock);

        messageKey = UUID.randomUUID().toString();

        orderToSend = OrderCreated.builder()
                .uuid(UUID.randomUUID()).message(UUID.randomUUID().toString())
                .build();

    }

    @Test
    void listen_success() throws ExecutionException, InterruptedException {
        orderCreatedConsumer.listen(0,messageKey,orderToSend);
        verify(dispatcherServMock).process(messageKey,orderToSend);
    }
    @Test
    void listen_ServiceThrowsException() throws ExecutionException, InterruptedException {
        doThrow(new RuntimeException("Service failure")).when(dispatcherServMock).process(messageKey,orderToSend);

        orderCreatedConsumer.listen(0,messageKey,orderToSend);
        verify(dispatcherServMock).process(messageKey,orderToSend);
    }
}