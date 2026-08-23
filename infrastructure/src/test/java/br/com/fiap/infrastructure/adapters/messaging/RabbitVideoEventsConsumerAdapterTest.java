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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitVideoEventsConsumerAdapterTest {

    @Mock
    private SendNotificationInputPort sendNotificationInputPort;

    private ObjectMapper objectMapper;

    private RabbitVideoEventsConsumerAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        adapter = new RabbitVideoEventsConsumerAdapter(sendNotificationInputPort, objectMapper);
    }

    @Test
    void shouldSendNotificationWhenMessageIsValid() {
        String message = "{\"videoId\":\"1\",\"eventType\":\"VIDEO_FAILED\"}";
        when(sendNotificationInputPort.send(any(VideoNotificationEvent.class))).thenReturn(new NotificationResult(true, "ok"));

        assertDoesNotThrow(() -> adapter.onVideoEvent(message, "video.events", "video.uploaded"));

        verify(sendNotificationInputPort).send(argThat(event ->
                "1".equals(event.getVideoId()) && "VIDEO_FAILED".equals(event.getEventType())));
    }

    @Test
    void shouldNotThrowWhenNotificationFails() {
        String message = "{\"videoId\":\"1\",\"eventType\":\"VIDEO_FAILED\"}";
        when(sendNotificationInputPort.send(any(VideoNotificationEvent.class))).thenReturn(new NotificationResult(false, "smtp failure"));

        assertDoesNotThrow(() -> adapter.onVideoEvent(message, "video.events", "video.failed"));

        verify(sendNotificationInputPort).send(argThat(event ->
                "1".equals(event.getVideoId()) && "VIDEO_FAILED".equals(event.getEventType())));
    }

    @Test
    void shouldThrowWhenMessageProcessingFails() {
        String message = "{invalid-json}";

        assertThrows(RuntimeException.class, () -> adapter.onVideoEvent(message, "video.events", "video.uploaded"));
    }
}
