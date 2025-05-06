package com.example.hotel.NotificationService.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_QUEUE = "booking_queue_Hazem_52_8936";
    public static final String EXCHANGE = "HazemElgammal_52_8936";
    public static final String BOOKING_ROUTING_KEY = "booking_routing_HazemElgammal_52_8936";

    @Bean
    public Queue bookingQueue() {
        return new Queue(BOOKING_QUEUE, false);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bookingBinding(Queue bookingQueue, TopicExchange bookingExchange) {
        return BindingBuilder
                .bind(bookingQueue)
                .to(bookingExchange)
                .with(BOOKING_ROUTING_KEY);
    }
}
