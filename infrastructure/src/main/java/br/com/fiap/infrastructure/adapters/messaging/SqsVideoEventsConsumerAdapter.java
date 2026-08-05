package br.com.fiap.infrastructure.adapters.messaging;

import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.in.SendNotificationInputPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true", matchIfMissing = false)
public class SqsVideoEventsConsumerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SqsVideoEventsConsumerAdapter.class);

    private final SqsClient sqsClient;
    private final SendNotificationInputPort sendNotification;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queues.video-events:http://localhost:4566/000000000000/video-events}")
    private String videoEventsQueueUrl;

    public SqsVideoEventsConsumerAdapter(SqsClient sqsClient,
                                         SendNotificationInputPort sendNotification,
                                         ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.sendNotification = sendNotification;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.polling.fixed-delay-ms:3000}")
    public void pollVideoEvents() {
        try {
            ReceiveMessageResponse response = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(videoEventsQueueUrl)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(5)
                            .build());

            for (Message message : response.messages()) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("[SQS-CONSUMER] Error polling video-events queue", e);
        }
    }

    private void processMessage(Message message) {
        try {
            log.debug("[SQS-CONSUMER] Received message id={}", message.messageId());
            VideoNotificationEvent event = objectMapper.readValue(message.body(), VideoNotificationEvent.class);
            log.info("[SQS-CONSUMER] Processing notification for videoId={} type={}", event.getVideoId(), event.getEventType());

            NotificationResult result = sendNotification.send(event);
            if (result == null || !result.isSuccess()) {
                log.warn("[SQS-CONSUMER] Notification failed for videoId={} message={}",
                        event.getVideoId(), result != null ? result.getMessage() : "null result");
                return;
            }

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(videoEventsQueueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

            log.info("[SQS-CONSUMER] Notification sent and message deleted for videoId={}", event.getVideoId());
        } catch (Exception ex) {
            log.error("[SQS-CONSUMER] Error processing message id={}: {}", message.messageId(), ex.getMessage(), ex);
        }
    }
}
