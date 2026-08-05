package br.com.fiap.infrastructure.adapters.email;

import br.com.fiap.domain.exceptions.NotificationSendException;
import br.com.fiap.domain.model.VideoNotificationEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SmtpEmailAdapter(mailSender);
        ReflectionTestUtils.setField(adapter, "fromEmail", "noreply@fiapx.com");
    }

    @Test
    void shouldSendFailureEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        adapter.sendFailureEmail(event("VIDEO_FAILED"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldSendSuccessEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        adapter.sendSuccessEmail(event("VIDEO_PROCESSED"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldThrowNotificationSendExceptionWhenMailSenderFails() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("smtp offline")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.sendFailureEmail(event("VIDEO_FAILED")))
                .isInstanceOf(NotificationSendException.class)
                .hasMessageContaining("smtp offline");
    }

    private VideoNotificationEvent event(String eventType) {
        return new VideoNotificationEvent(
                "550e8400-e29b-41d4-a716-446655440000",
                "user-1",
                "user@example.com",
                "video.mp4",
                eventType,
                "encoder error",
                "2026-01-01T10:00:00Z"
        );
    }
}
