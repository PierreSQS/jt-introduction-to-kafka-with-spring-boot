package dev.lydtech.dispatch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DispatchService {

    public void processOrder(String orderDetails) {
        log.info("Processing order: {}", orderDetails);
        // Process the order
    }


}
