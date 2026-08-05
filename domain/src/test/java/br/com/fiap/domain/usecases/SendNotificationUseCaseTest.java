package br.com.fiap.domain.usecases;

import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.out.NotificationEmailPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendNotificationUseCase tests")
class SendNotificationUseCaseTest {

    @Mock
    private NotificationEmailPort notificationEmailPort;

    @Test
    void shouldAlwaysSendFailureEmailForVideoFailed() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, false);

        NotificationResult result = useCase.send(event("VIDEO_FAILED"));

        verify(notificationEmailPort).sendFailureEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldSendSuccessEmailWhenProcessedNotificationEnabled() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, true);

        NotificationResult result = useCase.send(event("VIDEO_PROCESSED"));

        verify(notificationEmailPort).sendSuccessEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldNotSendSuccessEmailWhenProcessedNotificationDisabled() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, false);

        NotificationResult result = useCase.send(event("VIDEO_PROCESSED"));

        verify(notificationEmailPort, never()).sendSuccessEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("skipped");
    }

    @Test
    void shouldIgnoreUnknownTypeWithoutThrowingException() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, true);

        NotificationResult result = useCase.send(event("UNKNOWN"));

        verify(notificationEmailPort, never()).sendFailureEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));
        verify(notificationEmailPort, never()).sendSuccessEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldReturnSuccessWhenOperationSucceeds() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, true);

        NotificationResult result = useCase.send(event("VIDEO_PROCESSED"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("Success notification sent");
    }

    @Test
    void shouldReturnFailureWhenEmailSendingThrowsException() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, false);
        doThrow(new RuntimeException("smtp down")).when(notificationEmailPort)
                .sendFailureEmail(org.mockito.ArgumentMatchers.any(VideoNotificationEvent.class));

        NotificationResult result = useCase.send(event("VIDEO_FAILED"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("smtp down");
    }

    private VideoNotificationEvent event(String eventType) {
        return new VideoNotificationEvent(
                "550e8400-e29b-41d4-a716-446655440000",
                "user-1",
                "user@example.com",
                "video.mp4",
                eventType,
                "FFmpeg error",
                "2026-01-01T10:00:00Z"
        );
    }
}
