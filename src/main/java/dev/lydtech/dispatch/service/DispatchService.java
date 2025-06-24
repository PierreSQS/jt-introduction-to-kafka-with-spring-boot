package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DispatchService {

    private static final String ORDER_DISPATCH_TOPIC = "order.dispatched";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(OrderCreated payload) throws Exception {
        log.info("Processing order: {}", payload);

        // Process the order

        // 1. Create an OrderDispatched event
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(payload.getOrderId())
                .build();

        // 2. Send the order dispatched event to Kafka synchronously
        kafkaTemplate.send(ORDER_DISPATCH_TOPIC, orderDispatched).get();

        log.info("Dispatched order: {}", orderDispatched);
    }
}
