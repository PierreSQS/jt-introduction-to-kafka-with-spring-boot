package dev.lydtech.dispatch.service;

import java.util.concurrent.CompletableFuture;

import dev.lydtech.dispatch.event.DispatchPreparing;
import dev.lydtech.dispatch.event.OrderCreated;
import dev.lydtech.dispatch.event.OrderDispatched;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DispatchServiceTest {

    private DispatchService service;

    private KafkaTemplate <String, Object> kafkaTemplateMock;

    // Generate a random messageKey for the message
    // This messageKey can be used to correlate messages in Kafka
    private final String messageKey = randomUUID().toString();

    @BeforeEach
    void setUp() {
        kafkaTemplateMock = mock(KafkaTemplate.class);
        service = new DispatchService(kafkaTemplateMock);
    }

    @Test
    void process_Success() throws Exception {
        // Given
        
        // Mock the sending of the OrderDispatched event
        given(kafkaTemplateMock.send(eq(DispatchService.ORDER_DISPATCH_TOPIC), anyString(), any(OrderDispatched.class))).willReturn(mock(CompletableFuture.class));

        // Mock the sending of the DispatchPreparing event
        given(kafkaTemplateMock.send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), anyString(), any(DispatchPreparing.class))).willReturn(mock(CompletableFuture.class));

        OrderCreated orderCreatedEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        // When
        service.process(messageKey,orderCreatedEvent);

        // Then
        // Verify that the order event was sent to the 'order.dispatched' topic
        verify(kafkaTemplateMock, times(1))
                .send(DispatchService.ORDER_DISPATCH_TOPIC, messageKey,
                        OrderDispatched.builder()
                                .orderId(orderCreatedEvent.getOrderId())
                                .processedById(DispatchService.APPLICATION_ID)
                                .notes("Dispatched order with ID: " + orderCreatedEvent.getOrderId())
                                .build());

        // Verify that the dispatch preparing event was sent to the 'dispatch.tracking' topic
        verify(kafkaTemplateMock, times(1))
                .send(DispatchService.DISPATCH_TRACKING_TOPIC, messageKey,
                        DispatchPreparing.builder()
                                .orderId(orderCreatedEvent.getOrderId())
                                .build());
    }

    @Test
    void testProcess_OrderDispatchedProducerThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        // refined the error message
        doThrow(new RuntimeException("Order Dispatch Producer failure")).when(kafkaTemplateMock)
                .send(eq(DispatchService.ORDER_DISPATCH_TOPIC), eq(messageKey), any(OrderDispatched.class));

        assertThatThrownBy(() -> service.process(messageKey,testEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order Dispatch Producer failure");

        verify(kafkaTemplateMock, times(1))
                .send(eq(DispatchService.ORDER_DISPATCH_TOPIC), eq(messageKey), any(OrderDispatched.class));

        // Stop the execution after the first exception,
        // no further interaction with kafkaTemplateMock should occur
        verifyNoMoreInteractions(kafkaTemplateMock);
    }

    @Test
    void testProcess_DispatchPreparingProducerThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        // Mock the sending of the OrderDispatched event
        given(kafkaTemplateMock.send(eq(DispatchService.ORDER_DISPATCH_TOPIC), eq(messageKey), any(OrderDispatched.class)))
                .willReturn(mock(CompletableFuture.class));

        // refined the error message
        doThrow(new RuntimeException("Dispatch Tracking Producer failure")).when(kafkaTemplateMock)
                .send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), eq(messageKey), any(DispatchPreparing.class));

        assertThatThrownBy(() -> service.process(messageKey,testEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Dispatch Tracking Producer failure");

        // the 2 calls to kafkaTemplateMock should have been made
        verify(kafkaTemplateMock, times(1))
                .send(eq(DispatchService.ORDER_DISPATCH_TOPIC), eq(messageKey), any(OrderDispatched.class));

        verify(kafkaTemplateMock, times(1))
                .send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), eq(messageKey), any(DispatchPreparing.class));
    }
}
