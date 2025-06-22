package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreatedHandler {

    private final DispatchService dispatchService;

    @KafkaListener(
            id = "orderConsumerClient",
            topics = "order.created",
            groupId = "dispatch.order.create.consumer-group"
    )
    public void listen(String payload) {
        log.info("Received order-created event: {}", payload);

        // process Order Event
        dispatchService.processOrder(payload);
    }
}
