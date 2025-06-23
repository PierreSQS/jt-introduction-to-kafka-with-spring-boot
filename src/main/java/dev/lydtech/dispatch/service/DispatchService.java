package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DispatchService {

    public void processOrder(OrderCreated orderCreated) {
        log.info("Processing order: {}", orderCreated);
        // Process the order
    }


}
