package dev.lydtech.dispatch.integration;

import dev.lydtech.dispatch.config.KafkaConfig;
import dev.lydtech.dispatch.event.DispatchPreparing;
import dev.lydtech.dispatch.event.OrderCreated;
import dev.lydtech.dispatch.event.OrderDispatched;
import dev.lydtech.dispatch.service.DispatchService;
import dev.lydtech.dispatch.util.TestEventData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest(classes = {KafkaConfig.class, OrderDispatchIntegrationTest.TestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
class OrderDispatchIntegrationTest {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    @Autowired
    KafkaTemplate <String, Object> kafkaTemplate;

    @Autowired
    KafkaListenerContainer kafkaListenerContainer;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setUp() {
        kafkaListenerContainer.orderDispatchedCounter.set(0);
        kafkaListenerContainer.dispatchedPreparingCounter.set(0);

        kafkaListenerEndpointRegistry.getListenerContainers().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
    }

    @Test
    void testOrderDispatchFlow() throws Exception {
        // This test will verify the end-to-end flow of order dispatching
        // It will involve sending an OrderCreated event and verifying the
        // OrderDispatched and DispatchPreparing events are produced correctly.

        log.info("Starting Order Dispatch Integration Test...");

        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(randomUUID(), "test-order-item");

        log.info("Sending OrderCreated event: {}", orderCreated);
        sendEventMessage(ORDER_CREATED_TOPIC, orderCreated);

        // Wait for the events to be processed
        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListenerContainer.orderDispatchedCounter::get, equalTo(1));

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> kafkaListenerContainer.dispatchedPreparingCounter.get(),count -> count == 1);


        log.info("Order Dispatch Integration Test completed successfully.");
    }

    private void sendEventMessage(String topic, Object object) throws Exception {
        kafkaTemplate.send(MessageBuilder
                .withPayload(object)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public KafkaListenerContainer kafkaListenerContainer() {
            return new KafkaListenerContainer();
        }

        @Bean
        public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry() {
            return new KafkaListenerEndpointRegistry();
        }

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
        void onDispatchPreparing(final @Payload DispatchPreparing dispatchPreparing) {
            log.info("Received DispatchPreparing event: {}", dispatchPreparing);
            dispatchedPreparingCounter.incrementAndGet();
        }
    }
}
