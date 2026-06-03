package com.example.gameservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the RabbitMQ topology that Game Service publishes to.
 * Exchange "game.events" + routing key "move.played" + queue "move-played".
 * Bot Service binds to the same queue to consume the events.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "game.events";
    public static final String ROUTING_KEY = "move.played";
    public static final String QUEUE = "move-played";

    @Bean
    public TopicExchange gameExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue movePlayedQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding movePlayedBinding(Queue movePlayedQueue, TopicExchange gameExchange) {
        return BindingBuilder.bind(movePlayedQueue).to(gameExchange).with(ROUTING_KEY);
    }
}
