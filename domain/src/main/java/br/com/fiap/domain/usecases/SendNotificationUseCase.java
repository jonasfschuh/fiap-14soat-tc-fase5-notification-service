package br.com.fiap.domain.usecases;

import br.com.fiap.domain.enums.NotificationEventType;
import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.in.SendNotificationInputPort;
import br.com.fiap.domain.ports.out.NotificationEmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class SendNotificationUseCase implements SendNotificationInputPort {

    private static final Logger log = LoggerFactory.getLogger(SendNotificationUseCase.class);

    private final NotificationEmailPort notificationEmailPort;
    private final boolean notifyOnProcessed;

    public SendNotificationUseCase(NotificationEmailPort notificationEmailPort, boolean notifyOnProcessed) {
        this.notificationEmailPort = notificationEmailPort;
        this.notifyOnProcessed = notifyOnProcessed;
    }

    @Override
    public NotificationResult send(VideoNotificationEvent event) {
        NotificationEventType eventType = parseEventType(event != null ? event.getEventType() : null);
        if (eventType == null) {
            log.warn("Ignoring unsupported notification event type: {}", event != null ? event.getEventType() : null);
            return new NotificationResult(true, "Unsupported event type ignored");
        }

        try {
            if (eventType == NotificationEventType.VIDEO_FAILED) {
                notificationEmailPort.sendFailureEmail(event);
                return new NotificationResult(true, "Failure notification sent");
            }

            if (notifyOnProcessed) {
                notificationEmailPort.sendSuccessEmail(event);
                return new NotificationResult(true, "Success notification sent");
            }

            log.info("Skipping VIDEO_PROCESSED notification for videoId={} because notifyOnProcessed=false",
                    event != null ? event.getVideoId() : null);
            return new NotificationResult(true, "Success notification skipped");
        } catch (Exception ex) {
            log.error("Failed to send notification for videoId={}", event != null ? event.getVideoId() : null, ex);
            return new NotificationResult(false, "Failed to send notification: " + ex.getMessage());
        }
    }

    private NotificationEventType parseEventType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return null;
        }
        try {
            return NotificationEventType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
