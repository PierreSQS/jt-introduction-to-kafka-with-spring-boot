package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        String payloadToSend = "payload to send";
        orderCreatedConsumer.listen(payloadToSend);
        verify(dispatcherServMock).process(payloadToSend);
    }
}