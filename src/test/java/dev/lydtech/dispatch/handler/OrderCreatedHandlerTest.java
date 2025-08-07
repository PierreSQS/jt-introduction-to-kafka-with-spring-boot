package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.event.OrderCreated;
import dev.lydtech.dispatch.service.DispatchService;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OrderCreatedHandlerTest {

    private OrderCreatedHandler handler;
    private DispatchService dispatchServiceMock;

    // Generate a random key for the message
    // This key can be used to correlate messages in Kafka
    String messageKey = randomUUID().toString();

    @BeforeEach
    void setUp() {
        dispatchServiceMock = mock(DispatchService.class);
        handler = new OrderCreatedHandler(dispatchServiceMock);
    }

    @Test
    void listen_Success() throws Exception {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        handler.listen(0,messageKey, testEvent);

        verify(dispatchServiceMock, times(1)).process(messageKey, testEvent);
    }

    @Test
    void listen_ServiceThrowsException() throws Exception {

        // Simulate a service failure by throwing an exception when processing the event
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        doThrow(new RuntimeException("Service failure")).when(dispatchServiceMock).process(messageKey, testEvent);

        handler.listen(0,messageKey, testEvent);

        verify(dispatchServiceMock, times(1)).process(messageKey, testEvent);
    }
}
