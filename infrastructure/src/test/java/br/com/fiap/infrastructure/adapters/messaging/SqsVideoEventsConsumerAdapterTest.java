package br.com.fiap.infrastructure.adapters.messaging;

import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.in.SendNotificationInputPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsVideoEventsConsumerAdapterTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SendNotificationInputPort sendNotificationInputPort;

    @Mock
    private ObjectMapper objectMapper;

    private SqsVideoEventsConsumerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SqsVideoEventsConsumerAdapter(sqsClient, sendNotificationInputPort, objectMapper);
        ReflectionTestUtils.setField(adapter, "videoEventsQueueUrl", "http://localhost:4566/000000000000/video-events");
    }

    @Test
    void shouldSendNotificationAndDeleteMessageWhenMessageIsValid() throws Exception {
        Message message = Message.builder().messageId("msg-1").receiptHandle("rh-1").body("{\"videoId\":\"1\"}").build();
        VideoNotificationEvent event = new VideoNotificationEvent();
        event.setVideoId("1");
        event.setEventType("VIDEO_FAILED");

        when(sqsClient.receiveMessage(any(software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(objectMapper.readValue(message.body(), VideoNotificationEvent.class)).thenReturn(event);
        when(sendNotificationInputPort.send(event)).thenReturn(new NotificationResult(true, "ok"));

        adapter.pollVideoEvents();

        verify(sendNotificationInputPort).send(event);
        verify(sqsClient).deleteMessage(any(software.amazon.awssdk.services.sqs.model.DeleteMessageRequest.class));
    }

    @Test
    void shouldNotDeleteMessageWhenNotificationFails() throws Exception {
        Message message = Message.builder().messageId("msg-2").receiptHandle("rh-2").body("{\"videoId\":\"1\"}").build();
        VideoNotificationEvent event = new VideoNotificationEvent();
        event.setVideoId("1");
        event.setEventType("VIDEO_FAILED");

        when(sqsClient.receiveMessage(any(software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(objectMapper.readValue(message.body(), VideoNotificationEvent.class)).thenReturn(event);
        when(sendNotificationInputPort.send(event)).thenReturn(new NotificationResult(false, "smtp failure"));

        adapter.pollVideoEvents();

        verify(sendNotificationInputPort).send(event);
        verify(sqsClient, never()).deleteMessage(any(software.amazon.awssdk.services.sqs.model.DeleteMessageRequest.class));
    }

    @Test
    void shouldContinueWhenReceiveFails() {
        when(sqsClient.receiveMessage(any(software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest.class)))
                .thenThrow(new RuntimeException("sqs offline"));

        adapter.pollVideoEvents();

        verify(sqsClient).receiveMessage(any(software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest.class));
        verify(sendNotificationInputPort, never()).send(any(VideoNotificationEvent.class));
    }
}
