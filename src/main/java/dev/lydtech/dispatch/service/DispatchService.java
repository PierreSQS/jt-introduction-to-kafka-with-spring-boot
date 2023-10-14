package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.DispatchCompleted;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {
    public static final String ORDER_DISPATCHER_TOPIC = "order.dispatched";

    public static final String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    public static final UUID APPLICATION_ID = UUID.randomUUID();

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void process(String key, OrderCreated orderCreated)
            throws ExecutionException, InterruptedException {
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .uuid(orderCreated.getUuid())
                .processedBy(APPLICATION_ID)
                .notes("Dispatched: " + orderCreated.getUuid())
                .build();

        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                .orderId(orderCreated.getUuid())
                .build();

        DispatchCompleted dispatchCompleted = DispatchCompleted.builder()
                .orderID(orderCreated.getUuid())
                .date(LocalDate.now().toString())
                .build();

        kafkaTemplate.send(ORDER_DISPATCHER_TOPIC,key,orderDispatched).get();

        kafkaTemplate.send(DISPATCH_TRACKING_TOPIC,key, dispatchPreparing).get();

        kafkaTemplate.send(DISPATCH_TRACKING_TOPIC,key, dispatchCompleted).get();

        log.info("Sent message key={} -orderId={} - processed by={}",key, orderCreated.getUuid(), APPLICATION_ID);
    }
}
