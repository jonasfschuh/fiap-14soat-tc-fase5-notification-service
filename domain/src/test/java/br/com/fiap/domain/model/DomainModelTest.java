package br.com.fiap.domain.model;

import br.com.fiap.domain.exceptions.NotificationSendException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTest {

    @Test
    void shouldPopulateVideoNotificationEventFields() {
        VideoNotificationEvent event = new VideoNotificationEvent();
        event.setVideoId("video-1");
        event.setUserId("user-1");
        event.setUserEmail("user@example.com");
        event.setOriginalFilename("video.mp4");
        event.setEventType("VIDEO_FAILED");
        event.setErrorMessage("failure");
        event.setTimestamp("2026-01-01T10:00:00Z");

        assertThat(event.getVideoId()).isEqualTo("video-1");
        assertThat(event.getUserId()).isEqualTo("user-1");
        assertThat(event.getUserEmail()).isEqualTo("user@example.com");
        assertThat(event.getOriginalFilename()).isEqualTo("video.mp4");
        assertThat(event.getEventType()).isEqualTo("VIDEO_FAILED");
        assertThat(event.getErrorMessage()).isEqualTo("failure");
        assertThat(event.getTimestamp()).isEqualTo("2026-01-01T10:00:00Z");
    }

    @Test
    void shouldPopulateNotificationResultFields() {
        NotificationResult result = new NotificationResult();
        result.setSuccess(true);
        result.setMessage("sent");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("sent");
    }

    @Test
    void shouldCreateNotificationSendException() {
        NotificationSendException exception = new NotificationSendException("error", new RuntimeException("root"));

        assertThat(exception.getMessage()).contains("error");
        assertThat(exception.getCause()).hasMessage("root");
    }
}
