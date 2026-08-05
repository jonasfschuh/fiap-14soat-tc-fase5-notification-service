package br.com.fiap.domain.ports.in;

import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;

public interface SendNotificationInputPort {
    NotificationResult send(VideoNotificationEvent event);
}
