package dev.lydtech.dispatch.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrderCreated {
    private UUID uuid;
    private String message;
}
