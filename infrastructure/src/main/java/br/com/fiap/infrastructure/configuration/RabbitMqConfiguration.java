package br.com.fiap.infrastructure.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    public static final String VIDEO_EVENTS_EXCHANGE = "video.events";
    public static final String VIDEO_EVENTS_QUEUE = "video-events";


    @Bean
    public TopicExchange videoEventsExchange() {
        return new TopicExchange(VIDEO_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Declaração passiva: alinha com os argumentos já configurados no servidor RabbitMQ compartilhado:
     *   x-dead-letter-exchange    = video.events
     *   x-dead-letter-routing-key = video.events.dlq
     * Não redefine binding — isso é responsabilidade do serviço produtor.
     */
    @Bean
    public Queue videoEventsQueue() {
        return QueueBuilder.durable(VIDEO_EVENTS_QUEUE)
                .deadLetterExchange(VIDEO_EVENTS_EXCHANGE)
                .deadLetterRoutingKey("video.events.dlq")
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
