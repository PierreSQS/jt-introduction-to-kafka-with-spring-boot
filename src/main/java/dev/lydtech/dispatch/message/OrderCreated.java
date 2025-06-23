package dev.lydtech.dispatch.message;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreated {

    UUID orderId;

    String item;
}
