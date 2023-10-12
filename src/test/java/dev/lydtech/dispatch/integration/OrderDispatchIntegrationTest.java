package dev.lydtech.dispatch.integration;

import dev.lydtech.dispatch.config.DispatchConfig;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest(classes = DispatchConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = {"test"})
@EmbeddedKafka(controlledShutdown = true)
class OrderDispatchIntegrationTest {
    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaTestListener testListener;

    String messageKey;

    @Configuration
    static class TestConfig {
        @Bean
        public KafkaTestListener kafkaTestListener() {
            return new KafkaTestListener();
        }
    }


    /**
     * Use this receiver to consume messages from the outbound topics.
     */
    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
        void receiveDispatchPreparing(@Payload DispatchPreparing payload) {
            log.debug("Received DispatchPreparing: " + payload);
            dispatchPreparingCounter.incrementAndGet();
        }

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
        void receiveOrderDispatched(@Payload OrderDispatched payload) {
            log.debug("Received OrderDispatched: " + payload);
            orderDispatchedCounter.incrementAndGet();
        }
    }

    @BeforeEach
    void setUp() {
        testListener.dispatchPreparingCounter.set(0);
        testListener.orderDispatchedCounter.set(0);

        // Wait until the partitions are assigned.
        registry.getListenerContainers().forEach(messageListenerContainer ->
                ContainerTestUtils.waitForAssignment(messageListenerContainer,
                        embeddedKafkaBroker.getPartitionsPerTopic()));

        messageKey = UUID.randomUUID().toString();
    }

    /**
     * Send in an order.created event and ensure the expected outbound events are emitted.
     */
    @Test
    void testOrderDispatchFlow() throws Exception {

        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(),"test-item");
        send(ORDER_CREATED_TOPIC,messageKey,orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100,TimeUnit.MILLISECONDS)
                .until(testListener.dispatchPreparingCounter::get, equalTo(1));

        await().atMost(3,TimeUnit.SECONDS).pollDelay(100,TimeUnit.MILLISECONDS)
                .until(testListener.dispatchPreparingCounter::get,equalTo(1));
    }

    private void send(String topic, String messageKey, Object data) throws Exception{
        kafkaTemplate.send(MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .setHeader(KafkaHeaders.RECEIVED_KEY,messageKey)
                .build()).get();
    }
}
