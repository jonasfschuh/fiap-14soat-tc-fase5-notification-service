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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitVideoEventsConsumerAdapterTest {

    @Mock
    private SendNotificationInputPort sendNotificationInputPort;

    @Mock
    private ObjectMapper objectMapper;

    private RabbitVideoEventsConsumerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RabbitVideoEventsConsumerAdapter(sendNotificationInputPort, objectMapper);
    }

    @Test
    void shouldSendNotificationWhenMessageIsValid() throws Exception {
        String message = "{\"videoId\":\"1\"}";
        VideoNotificationEvent event = new VideoNotificationEvent();
        event.setVideoId("1");
        event.setEventType("VIDEO_FAILED");

        when(objectMapper.readValue(message, VideoNotificationEvent.class)).thenReturn(event);
        when(sendNotificationInputPort.send(event)).thenReturn(new NotificationResult(true, "ok"));

        assertDoesNotThrow(() -> adapter.onVideoEvent(message));

        verify(sendNotificationInputPort).send(event);
    }

    @Test
    void shouldNotThrowWhenNotificationFails() throws Exception {
        String message = "{\"videoId\":\"1\"}";
        VideoNotificationEvent event = new VideoNotificationEvent();
        event.setVideoId("1");
        event.setEventType("VIDEO_FAILED");

        when(objectMapper.readValue(message, VideoNotificationEvent.class)).thenReturn(event);
        when(sendNotificationInputPort.send(event)).thenReturn(new NotificationResult(false, "smtp failure"));

        assertDoesNotThrow(() -> adapter.onVideoEvent(message));

        verify(sendNotificationInputPort).send(event);
    }

    @Test
    void shouldThrowWhenMessageProcessingFails() throws Exception {
        String message = "{\"videoId\":\"1\"}";

        when(objectMapper.readValue(message, VideoNotificationEvent.class))
                .thenThrow(new RuntimeException("invalid payload"));

        assertThrows(RuntimeException.class, () -> adapter.onVideoEvent(message));
    }
}
