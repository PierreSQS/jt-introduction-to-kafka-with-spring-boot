package dev.lydtech.dispatch.integration;

import dev.lydtech.dispatch.event.OrderDispatched;
import dev.lydtech.dispatch.service.DispatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
class OrderDispatchIntegrationTest {

    @Test
    void testOrderDispatchFlow() throws Exception {
        // This test will verify the end-to-end flow of order dispatching
        // It will involve sending an OrderCreated event and verifying the
        // OrderDispatched and DispatchPreparing events are produced correctly.

        log.info("Starting Order Dispatch Integration Test...");



        log.info("Order Dispatch Integration Test completed successfully.");
    }

    // Kafka Listener Container
    public static class KafkaListenerContainer {

        AtomicInteger dispatchedPreparingCounter = new AtomicInteger(0);

        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = DispatchService.ORDER_DISPATCH_TOPIC)
        void onOrderDispatched(final @Payload OrderDispatched orderDispatched) {
            log.info("Received OrderDispatched event: {}", orderDispatched);
            orderDispatchedCounter.incrementAndGet();
        }

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = DispatchService.DISPATCH_TRACKING_TOPIC)
        void onDispatchPreparing(final @Payload OrderDispatched dispatchPreparing) {
            log.info("Received DispatchPreparing event: {}", dispatchPreparing);
            dispatchedPreparingCounter.incrementAndGet();
        }
    }
}
