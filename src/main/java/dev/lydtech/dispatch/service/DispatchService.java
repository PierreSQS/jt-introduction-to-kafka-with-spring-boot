package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DispatchService {

    public void process(OrderCreated payload) {
        log.info("Processing order: {}", payload);
        // Process the order
    }
}
