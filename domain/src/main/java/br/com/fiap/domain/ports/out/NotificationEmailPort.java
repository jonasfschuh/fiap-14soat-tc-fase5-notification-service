package br.com.fiap.domain.ports.out;

import br.com.fiap.domain.model.VideoNotificationEvent;

public interface NotificationEmailPort {
    void sendFailureEmail(VideoNotificationEvent event);

    void sendSuccessEmail(VideoNotificationEvent event);
}
