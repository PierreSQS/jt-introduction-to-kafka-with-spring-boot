package dev.lydtech.dispatch.service;

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

class DispatcherServiceTest {

    public static final String PRODUCER_FAILURE = "Producer failure";
    DispatcherService dispatcherService;

    KafkaTemplate<String,Object> kafkaProducerMock;

    OrderCreated orderCreatedTestEvent;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock();
        dispatcherService = new DispatcherService(kafkaProducerMock);

        orderCreatedTestEvent =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());
    }

    @Test
    void process_Success() throws ExecutionException, InterruptedException {
        given(kafkaProducerMock.send(anyString(), any(DispatchPreparing.class)))
                .willReturn(mock()); // note the mock-method without class arg!!!!

        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class)))
                .thenReturn(mock());

        dispatcherService.process(orderCreatedTestEvent);

        verify(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC), any(OrderDispatched.class));

        verify(kafkaProducerMock).send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC), any(DispatchPreparing.class));
    }

    @Test
    void process_OrderProducerThrowsException() {
        doThrow(new RuntimeException(PRODUCER_FAILURE))
                .when(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class));

        // AssertJ Exception Assertion
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> dispatcherService.process(orderCreatedTestEvent))
                .withMessage(PRODUCER_FAILURE);

        verify(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class));

        verifyNoMoreInteractions(kafkaProducerMock);

    }

    @Test
    void process_TrackingProducerThrowsException() {

        given(kafkaProducerMock.send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class)))
                .willReturn(mock());

        doThrow(new RuntimeException(PRODUCER_FAILURE))
                .when(kafkaProducerMock).send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC),any(DispatchPreparing.class));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> dispatcherService.process(orderCreatedTestEvent))
                .withMessage(PRODUCER_FAILURE);

        verify(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class));

        verify(kafkaProducerMock).send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC), any(DispatchPreparing.class));

    }
}