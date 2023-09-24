package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class DispatcherService {
    private static final String ORDER_DISPATCHER_TOPIC = "order.dispatched";

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void process(OrderCreated orderCreated) throws ExecutionException, InterruptedException {
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .uuid(orderCreated.getUuid())
                .build();

        kafkaTemplate.send(ORDER_DISPATCHER_TOPIC,orderDispatched).get();
    }
}
