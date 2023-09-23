package dev.lydtech.dispatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatcherServiceTest {

    DispatcherService dispatcherService;

    @BeforeEach
    void setUp() {
        dispatcherService = new DispatcherService();
    }

    @Test
    void process() {
        dispatcherService.process("payload Test");
    }
}