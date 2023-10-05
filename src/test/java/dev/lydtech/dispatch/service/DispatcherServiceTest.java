package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class DispatcherServiceTest {

    DispatcherService dispatcherService;

    KafkaTemplate<String,Object> kafkaProducerMock;

    OrderCreated orderCreatedTestEvent;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        dispatcherService = new DispatcherService(kafkaProducerMock);

        orderCreatedTestEvent =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());
    }

    @Test
    void process_Success() throws ExecutionException, InterruptedException {
        given(kafkaProducerMock.send(anyString(), any(DispatchPreparing.class)))
                .willReturn(mock()); // note the mock-method without class arg!!!!

        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class)))
                .thenReturn(mock(CompletableFuture.class));

        dispatcherService.process(orderCreatedTestEvent);

        verify(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC), any(OrderDispatched.class));
    }

    @Test
    void process_OrderServiceThrowsException() {
        when(kafkaProducerMock.send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC),any(DispatchPreparing.class)))
                .thenReturn(mock(CompletableFuture.class));

        doThrow(new RuntimeException("Producer failure"))
                .when(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class));

        Exception exception = assertThrows(RuntimeException.class,
                () -> dispatcherService.process(orderCreatedTestEvent));

        verify(kafkaProducerMock).send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class));

        // Extra AssertJ assertion to practise
        assertThat(exception.getMessage()).isEqualTo("Producer failure");
    }

    @Test
    void process_TrackingServiceThrowsException() {
        given(kafkaProducerMock.send(eq(DispatcherService.ORDER_DISPATCHER_TOPIC),any(OrderDispatched.class)))
                .willReturn(mock(CompletableFuture.class));


        doThrow(new RuntimeException("Producer failure"))
                .when(kafkaProducerMock).send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC),any(DispatchPreparing.class));

        Exception exception = assertThrows(RuntimeException.class,
                () -> dispatcherService.process(orderCreatedTestEvent));

        verify(kafkaProducerMock).send(eq(DispatcherService.DISPATCH_TRACKING_TOPIC),any(DispatchPreparing.class));

        // Extra AssertJ assertion to practise
        assertThat(exception.getMessage()).isEqualTo("Producer failure");
    }
}