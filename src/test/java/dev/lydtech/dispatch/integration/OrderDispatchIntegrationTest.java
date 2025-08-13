package dev.lydtech.dispatch.integration;

import dev.lydtech.dispatch.config.KafkaConfig;
import dev.lydtech.dispatch.event.DispatchCompleted;
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
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest(classes = {KafkaConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(kraft = true, controlledShutdown = true)
class OrderDispatchIntegrationTest {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    @Autowired
    KafkaTemplate <String, Object> kafkaTemplate;

    @Autowired
    KafkaTestListener kafkaTestListener;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setUp() {
        kafkaTestListener.orderDispatchedCounter.set(0);
        kafkaTestListener.dispatchedPreparingCounter.set(0);
        kafkaTestListener.dispatchCompletedCounter.set(0);

        // Wait until the partitions are assigned.
        // The application listener container has one topic and the test
        // listener container has multiple topics,
        // so take that into account when waiting for topic assignment.
        kafkaListenerEndpointRegistry.getListenerContainers()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container,
                        Objects.requireNonNull(container.getContainerProperties().getTopics()).length
                                * embeddedKafkaBroker.getPartitionsPerTopic()));
    }

    @Test
    void testOrderDispatchFlow() throws Exception {
        // This test will verify the end-to-end flow of order dispatching
        // It will involve sending an OrderCreated event and verifying the
        // OrderDispatched and DispatchPreparing events are produced correctly.

        log.info("Starting Order Dispatch Integration Test...");

        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(randomUUID(), "test-order-item");
        String msgKey = randomUUID().toString();

        log.info("Sending OrderCreated event: {}", orderCreated);
        sendEventMessage(msgKey, orderCreated);

        // Wait for the OrderDispatched event to be processed
        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaTestListener.orderDispatchedCounter::get, equalTo(1));

        // Wait for the DispatchPreparing event to be processed
        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> kafkaTestListener.dispatchedPreparingCounter.get(), count -> count == 1);

        // Wait for the DispatchCompleted event to be processed
        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> kafkaTestListener.dispatchCompletedCounter.get(), equalTo(1));


        log.info("Order Dispatch Integration Test completed successfully.");
    }

    private void sendEventMessage(String msgKey, Object object) throws Exception {
        kafkaTemplate.send(MessageBuilder
                .withPayload(object)
                .setHeader(KafkaHeaders.TOPIC,
                        OrderDispatchIntegrationTest.ORDER_CREATED_TOPIC)
                .setHeader(KafkaHeaders.KEY, msgKey)
                .build()).get();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public KafkaTestListener kafkaListenerContainer() {
            return new KafkaTestListener();
        }

    }

    // Kafka Listener Container
    // Kafka Listener to listen for OrderDispatched and DispatchPreparing events
    // This listener will be used to validate the OrderDispatched and DispatchPreparing events
    @KafkaListener(groupId = "KafkaIntegrationTest", topics = {DispatchService.DISPATCH_TRACKING_TOPIC,
            DispatchService.ORDER_DISPATCH_TOPIC})
    public static class KafkaTestListener {

        AtomicInteger dispatchedPreparingCounter = new AtomicInteger(0);

        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);

        AtomicInteger dispatchCompletedCounter = new AtomicInteger(0);

        // Kafka Listener to listen for OrderDispatched events
        // This listener will be used to validate the OrderDispatched event
        @KafkaHandler
        void onOrderDispatched(@Header(KafkaHeaders.RECEIVED_KEY) String msgKey,final @Payload OrderDispatched orderDispatched) {
            log.info("Received key {} and OrderDispatched event: {}", msgKey, orderDispatched);

            // Validate the received message key and event
            assertThat(msgKey).isNotBlank();
            assertThat(orderDispatched).isNotNull();
            orderDispatchedCounter.incrementAndGet();
        }

        @KafkaHandler
        void onDispatchPreparing(@Header(KafkaHeaders.RECEIVED_KEY) String msgKey, final @Payload DispatchPreparing dispatchPreparing) {
            log.info("Received key {} and DispatchPreparing event: {}", msgKey, dispatchPreparing);

            // Validate the received message key and event
            assertThat(msgKey).isNotNull();
            assertThat(dispatchPreparing).isNotNull();
            dispatchedPreparingCounter.incrementAndGet();
        }

        // Kafka Listener to listen for DispatchCompleted events
        // This listener will be used to validate the DispatchCompleted event
        @KafkaHandler
        void onDispatchCompleted(@Header(KafkaHeaders.RECEIVED_KEY) String msgKey, final @Payload DispatchCompleted dispatchCompleted) {
            log.info("Received key {} and DispatchCompleted event: {}", msgKey, dispatchCompleted);

            // Validate the received message key and event
            assertThat(msgKey).isNotNull();
            assertThat(dispatchCompleted).isNotNull();
            dispatchCompletedCounter.incrementAndGet();
        }
    }
}
