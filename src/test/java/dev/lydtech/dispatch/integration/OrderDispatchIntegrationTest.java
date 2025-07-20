package dev.lydtech.dispatch.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
}
