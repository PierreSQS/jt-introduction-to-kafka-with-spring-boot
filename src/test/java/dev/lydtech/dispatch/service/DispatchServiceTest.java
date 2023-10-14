package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.DispatchCompleted;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class DispatchServiceTest {

    public static final String PRODUCER_FAILURE = "Producer failure";
    DispatchService dispatchService;

    KafkaTemplate<String,Object> kafkaProducerMock;

    OrderCreated orderCreatedTestEvent;

    String messageKey;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock();
        dispatchService = new DispatchService(kafkaProducerMock);

        orderCreatedTestEvent =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());

        messageKey = UUID.randomUUID().toString();
    }

    @Test
    void process_Success() throws ExecutionException, InterruptedException {
        given(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class)))
                .willReturn(mock());

        given(kafkaProducerMock.send(anyString(), anyString(), any(DispatchCompleted.class)))
                .willReturn(mock());

        when(kafkaProducerMock.send(anyString(), anyString(), any(OrderDispatched.class)))
                .thenReturn(mock());

        dispatchService.process(messageKey,orderCreatedTestEvent);

        verify(kafkaProducerMock).send(eq(DispatchService.ORDER_DISPATCHER_TOPIC), anyString(), any(OrderDispatched.class));

        verify(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), anyString(), any(DispatchPreparing.class));
    }

    @Test
    void process_OrderProducerThrowsException() {
        doThrow(new RuntimeException(PRODUCER_FAILURE))
                .when(kafkaProducerMock).send(eq(DispatchService.ORDER_DISPATCHER_TOPIC), anyString(), any(OrderDispatched.class));

        // AssertJ Exception Assertion
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> dispatchService.process(messageKey,orderCreatedTestEvent))
                .withMessage(PRODUCER_FAILURE);

        verify(kafkaProducerMock).send(eq(DispatchService.ORDER_DISPATCHER_TOPIC),anyString(), any(OrderDispatched.class));

        verifyNoMoreInteractions(kafkaProducerMock);

    }

    @Test
    void process_TrackingProducerThrowsExceptionWhenSendingDispatchPreparingEvent() {

        given(kafkaProducerMock.send(eq(DispatchService.ORDER_DISPATCHER_TOPIC),anyString(), any(OrderDispatched.class)))
                .willReturn(mock());

        doThrow(new RuntimeException(PRODUCER_FAILURE))
                .when(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC),anyString(), any(DispatchPreparing.class));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> dispatchService.process(messageKey,orderCreatedTestEvent))
                .withMessage(PRODUCER_FAILURE);

        verify(kafkaProducerMock).send(eq(DispatchService.ORDER_DISPATCHER_TOPIC),anyString(), any(OrderDispatched.class));

        verify(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC),anyString(), any(DispatchPreparing.class));

        verifyNoMoreInteractions(kafkaProducerMock);

    }

    @Test
    void process_TrackingProducerThrowsExceptionWhenSendingDispatchCompletedEvent() {
        given(kafkaProducerMock.send(eq(DispatchService.ORDER_DISPATCHER_TOPIC),
                eq(messageKey), any(OrderDispatched.class))).willReturn(mock());

        given(kafkaProducerMock.send(eq(DispatchService.DISPATCH_TRACKING_TOPIC),
                eq(messageKey), any(DispatchPreparing.class))).willReturn(mock());

        doThrow(new RuntimeException(PRODUCER_FAILURE)).when(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC),
                eq(messageKey),any(DispatchCompleted.class));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> dispatchService.process(messageKey, orderCreatedTestEvent))
                .withMessage(PRODUCER_FAILURE);

        verify(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), eq(messageKey),
                any(DispatchPreparing.class));

        verify(kafkaProducerMock).send(eq(DispatchService.ORDER_DISPATCHER_TOPIC),eq(messageKey),
                any(OrderDispatched.class));

        verify(kafkaProducerMock).send(eq(DispatchService.DISPATCH_TRACKING_TOPIC), eq(messageKey),
                any(DispatchCompleted.class));
    }
}