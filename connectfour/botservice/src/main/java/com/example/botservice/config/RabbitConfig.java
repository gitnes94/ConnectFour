package com.example.botservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bot Service consumes from the same queue Game Service publishes to.
 * Declaring the queue here (durable) makes the consumer self-sufficient even
 * if it starts before Game Service has declared the topology.
 */
@Configuration
public class RabbitConfig {

    public static final String QUEUE = "move-played";

    @Bean
    public Queue movePlayedQueue() {
        return new Queue(QUEUE, true);
    }
}
