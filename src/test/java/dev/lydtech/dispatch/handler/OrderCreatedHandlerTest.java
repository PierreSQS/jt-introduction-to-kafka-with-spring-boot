package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        String payload = "order details";

        // When
        orderCreateHandler.listen(payload);

        // Then

        // Verify that the dispatch service processes the order
        verify(dispatchServMock,times(1)).processOrder(payload);
    }
}