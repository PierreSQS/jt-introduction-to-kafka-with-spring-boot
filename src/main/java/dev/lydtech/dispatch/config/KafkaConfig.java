package dev.lydtech.dispatch.config;

import dev.lydtech.dispatch.message.OrderCreated;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // create a Kafka Listener container factory Bean
    /**
     * Creates a ConcurrentKafkaListenerContainerFactory for Kafka consumers with generic types String for the key and
     * Object for the value. This factory is used in Spring Kafka to create listener containers
     * that handle consuming messages from Kafka topics.
     *
     * @param consumerFactory the ConsumerFactory to use for creating consumers
     * @return a ConcurrentKafkaListenerContainerFactory configured with the provided ConsumerFactory
     */
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // Set the consumer factory
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    // create a ConsumerFactory Bean
    /**
     * Creates a ConsumerFactory for Kafka consumers with generic types String for the key and Object for the value.
     * This factory is used to create Kafka consumers that can consume messages from Kafka topics.
     *
     * @return a ConsumerFactory configured for consuming messages with String keys and Object values
     */
     @Bean
     public ConsumerFactory<String, Object> consumerFactory(
             @Value("${kafka.bootstrap-servers}") String bootstrapServers) {

         Map<String, Object> props = new HashMap<>();

         // Set the necessary properties for the consumer
         props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
         props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
         props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreated.class.getName());

         // Not in use at the moment but can be used to deserialize the value
         props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         return new DefaultKafkaConsumerFactory<>(props);
     }
}
