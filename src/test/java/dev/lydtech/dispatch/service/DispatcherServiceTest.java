package dev.lydtech.dispatch.service;

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
import static org.mockito.Mockito.*;

class DispatcherServiceTest {

    DispatcherService dispatcherService;

    KafkaTemplate<String,Object> kafkaProducerMock;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        dispatcherService = new DispatcherService(kafkaProducerMock);
    }

    @Test
    void process_Success() throws ExecutionException, InterruptedException {
        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));

        OrderCreated orderCreatedTestEven =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());

        dispatcherService.process(orderCreatedTestEven);

        verify(kafkaProducerMock).send(eq("order.dispatched"), any(OrderDispatched.class));
    }

    @Test
    void process_ServiceThrowsException() throws ExecutionException, InterruptedException {
        doThrow(new RuntimeException("Producer failure"))
                .when(kafkaProducerMock).send(eq("order.dispatched"),any(OrderDispatched.class));

        OrderCreated orderCreatedTestEven =
                new OrderCreated(UUID.randomUUID(),UUID.randomUUID().toString());


        Exception exception = assertThrows(RuntimeException.class,
                () -> dispatcherService.process(orderCreatedTestEven));

        verify(kafkaProducerMock).send(eq("order.dispatched"),any(OrderDispatched.class));

        assertThat(exception.getMessage()).isEqualTo("Producer failure");

    }
}