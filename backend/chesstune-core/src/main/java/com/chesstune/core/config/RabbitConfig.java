package com.chesstune.core.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration on the Core (publisher) side.
 * Declares the same exchange/queue/binding to ensure they exist.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "chesstune.exchange";
    public static final String QUEUE = "chesstune.game.analysis";
    public static final String ROUTING_KEY = "game.completed";

    @Bean
    public TopicExchange chessTuneExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding binding(Queue analysisQueue, TopicExchange chessTuneExchange) {
        return BindingBuilder.bind(analysisQueue)
                .to(chessTuneExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
