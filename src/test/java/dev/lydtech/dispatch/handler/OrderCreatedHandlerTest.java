package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OrderCreateHandlerTest {

    OrderCreatedHandler orderCreateHandler;

    DispatchService dispatchServMock;

    @BeforeEach
    void setUp() {
        dispatchServMock = mock(DispatchService.class);
        orderCreateHandler = new OrderCreatedHandler(dispatchServMock);
    }

    @Test
    void listen() {
        // Given
        OrderCreated orderCreated = OrderCreated.builder()
                .orderId(randomUUID())
                .item(randomUUID().toString())
                .build();

        // When
        orderCreateHandler.listen(orderCreated);

        // Then

        // Verify that the dispatch service processes the order
        verify(dispatchServMock,times(1)).processOrder(orderCreated);
    }
}