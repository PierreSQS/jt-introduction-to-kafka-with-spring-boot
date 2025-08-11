package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.event.DispatchCompleted;
import dev.lydtech.dispatch.event.DispatchPreparing;
import dev.lydtech.dispatch.event.OrderCreated;
import dev.lydtech.dispatch.event.OrderDispatched;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DispatchService {

    public static final String ORDER_DISPATCH_TOPIC = "order.dispatched";

    public static final String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    public static final UUID APPLICATION_ID = UUID.randomUUID();

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(String key, OrderCreated payload) throws Exception {
        log.info("Processing order: {}", payload);

        // Process the order

        // 1a. Create an OrderDispatched event
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(payload.getOrderId())
                .processedById(APPLICATION_ID)
                .notes("Dispatched order with ID: " + payload.getOrderId())
                .build();

        // 1b. Create a DispatchPreparing event
        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                .orderId(payload.getOrderId())
                .build();

        // 1c. Create a DispatchCompleted event
        DispatchCompleted dispatchCompleted = DispatchCompleted.builder()
                .orderId(payload.getOrderId())
                .dateCompleted(LocalDateTime.now().toString())
                .build();


        // 2. Send the order dispatched event to the 'order.dispatched' topic in Kafka synchronously
        kafkaTemplate.send(ORDER_DISPATCH_TOPIC, key, orderDispatched).get();

        // 3. Log the dispatched order event
        log.info("key {}, OrderDispatched: {} sent", key, orderDispatched);

        // 4. Send the dispatch preparing event to the 'dispatch.tracking' topic in Kafka synchronously
        kafkaTemplate.send(DISPATCH_TRACKING_TOPIC, key, dispatchPreparing).get();

        // 5. Log the dispatch preparing event
        log.info("key {}, DispatchPreparing: {} sent", key, dispatchPreparing);

        // 6. Send the dispatch completed event to the 'dispatch.tracking' topic in Kafka synchronously
        kafkaTemplate.send(DISPATCH_TRACKING_TOPIC, key, dispatchCompleted).get();

        // 7. Log the dispatch completed event
        log.info("key {}, DispatchCompleted: {} sent", key, dispatchCompleted);

    }
}
