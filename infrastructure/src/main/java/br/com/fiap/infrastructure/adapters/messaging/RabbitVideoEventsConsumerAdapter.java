package br.com.fiap.infrastructure.adapters.messaging;

import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.in.SendNotificationInputPort;
import br.com.fiap.infrastructure.configuration.RabbitMqConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Consome eventos video-events do RabbitMQ e envia notificações por e-mail. */
@Component
public class RabbitVideoEventsConsumerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RabbitVideoEventsConsumerAdapter.class);

    private final SendNotificationInputPort sendNotification;
    private final ObjectMapper objectMapper;

    public RabbitVideoEventsConsumerAdapter(SendNotificationInputPort sendNotification,
                                            ObjectMapper objectMapper) {
        this.sendNotification = sendNotification;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConfiguration.VIDEO_EVENTS_QUEUE)
    public void onVideoEvent(
            String message,
            @Header(name = AmqpHeaders.RECEIVED_EXCHANGE, required = false) String exchange,
            @Header(name = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
        try {
            log.info(">>> Payload recebido ao RabbitMQ — exchange [{}] routing-key [{}]:\n{}",
                    exchange != null ? exchange : "unknown",
                    routingKey != null ? routingKey : "unknown",
                    formatPayload(message));
            VideoNotificationEvent event = objectMapper.readValue(message, VideoNotificationEvent.class);
            log.info("[RabbitMQ] Processing notification for videoId={} type={}",
                    event.getVideoId(), event.getEventType());

            NotificationResult result = sendNotification.send(event);
            if (result == null || !result.isSuccess()) {
                log.warn("[RabbitMQ] Notification failed for videoId={} message={}",
                        event.getVideoId(), result != null ? result.getMessage() : "null result");
                return;
            }
            log.info("[RabbitMQ] Notification sent for videoId={}", event.getVideoId());
        } catch (Exception e) {
            log.error("[RabbitMQ] Error processing video-events message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process video-events message", e);
        }
    }

    private String formatPayload(String message) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectMapper.readTree(message));
        } catch (JsonProcessingException e) {
            return message;
        }
    }
}
