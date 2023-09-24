package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreatedConsumer {

    private final DispatcherService dispatcherService;

    @KafkaListener(
            id = "orderConsumerClient",
            topics = "order.created",
            groupId = "dispatch.order.created.consumer",
            containerFactory = "kafkaListenerContainerFactory"

    ) public void listen(OrderCreated payload) {
        log.info("Received message - payload: {}",payload);

        try {
            dispatcherService.process(payload);
        } catch(Exception e) {
            log.error("Processing failure",e);
        }

    }
}
