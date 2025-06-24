package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DispatchServiceTest {

    private DispatchService service;

    private KafkaTemplate <String, Object> kafkaTemplateMock;

    @BeforeEach
    void setUp() {
        service = new DispatchService();
        kafkaTemplateMock = mock(KafkaTemplate.class);
    }

    @Test
    void process_Success() throws Exception {
        // Given
        given(kafkaTemplateMock.send(anyString(), any(OrderDispatched.class))).willReturn(mock(CompletableFuture.class));

        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        // When
        service.process(testEvent);

        // Then, Verify that the order was processed correctly
        verify(kafkaTemplateMock, times(1))
                .send("order.dispatched", OrderDispatched.builder()
                        .orderId(testEvent.getOrderId())
                        .build());
    }
}
