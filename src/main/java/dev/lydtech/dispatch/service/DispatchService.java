package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.event.DispatchPreparing;
import dev.lydtech.dispatch.event.OrderCreated;
import dev.lydtech.dispatch.event.OrderDispatched;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DispatchService {

    private static final String ORDER_DISPATCH_TOPIC = "order.dispatched";

    private static final String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(OrderCreated payload) throws Exception {
        log.info("Processing order: {}", payload);

        // Process the order

        // 1a. Create an OrderDispatched event
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(payload.getOrderId())
                .build();

        // 1b. Create a DispatchPreparing event
        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                .orderId(payload.getOrderId())
                .build();

        // 2. Send the order dispatched event to the 'order.dispatched' topic in Kafka synchronously
        kafkaTemplate.send(ORDER_DISPATCH_TOPIC, orderDispatched).get();

        // 3. Log the dispatched order event
        log.info("Dispatched order: {} send", orderDispatched);

        // 4. Send the dispatch preparing event to the 'dispatch.tracking' topic in Kafka synchronously
        kafkaTemplate.send(DISPATCH_TRACKING_TOPIC, dispatchPreparing).get();

        // 5. Log the dispatch preparing event
        log.info("DispatchPreparing: {} send", dispatchPreparing);
    }
}
